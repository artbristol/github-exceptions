package eu.ocathain.github.exceptions.github;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import eu.ocathain.github.exceptions.pmd.FileAnalyser;
import eu.ocathain.github.exceptions.pmd.PmdProblem;
import net.sourceforge.pmd.RuleViolation;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.UncheckedException;
import org.jooq.lambda.fi.util.function.CheckedPredicate;
import org.jooq.lambda.tuple.Tuple2;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHRateLimit;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

@Component
public class GithubSlurper {

    private static final Logger logger = LoggerFactory.getLogger(GithubSlurper.class);

    public static final CheckedPredicate<Tuple2<GHEventPayload.Push.PushCommit, GHCommit>> NON_MERGE_COMMITS =
            tuple -> tuple.v2.getParents().size() == 1;

    private static final CheckedPredicate<GHEventInfo> JAVA_PROJECTS =
            event -> event.getRepository().getLanguage() != null
                    && event.getRepository().getLanguage().toLowerCase(Locale.ENGLISH).equals("java");


    private OkHttpClient okClient = new OkHttpClient();

    @Autowired
    private RecentCommits recentCommits;

    @Autowired
    private FileAnalyser fileAnalyser;

    private volatile boolean running = false;
    private GitHub github;

    @PostConstruct
    void setupGithub() throws IOException {
        Cache cache = new Cache(Files.createTempDirectory("github-exceptions").toFile(), 10 * 1024 * 1024); // 10MB cache
        github = GitHubBuilder.fromCredentials()
                .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(cache))))
                .build();
    }

    @Scheduled(fixedRate = 1000)
    public void analyseCommits() throws IOException {
        if (!running) {
            running = true;

            try {
                while (true) {
                    try {
                        parseRecentEvents();
                    } catch (UncheckedException ex) {
                        logger.error("Exception parsing events", ex);
                    }
                }
            } finally {
                running = false;
            }
        }
    }

    private void parseRecentEvents() throws IOException {
        github.getEvents().stream()
                .peek(Unchecked.consumer(action -> {
                    GHRateLimit rateLimit = github.getRateLimit();

                    if (((double) rateLimit.remaining) / ((double) rateLimit.limit) < 0.1f) {
                        logger.warn("Rate limit {}; {}", rateLimit.limit, rateLimit.remaining);
                    }
                    while (rateLimit.remaining < 100) {
                        Thread.sleep(10000);
                        logger.info("Rate limited!");
                        rateLimit = github.getRateLimit();
                    }

                }))
                .filter(event -> event.getType() == GHEvent.PUSH)
                .filter(Unchecked.predicate(JAVA_PROJECTS))
                .map(Unchecked.function(event -> new Tuple2<>(event, event.getPayload(GHEventPayload.Push.class))))
                .forEach(Unchecked.consumer(tuple -> {
                    GHEventInfo event = tuple.v1;
                    GHEventPayload.Push push = tuple.v2;
                    logger.debug(
                            "Event repository [{}]; commits [{}]",
                            new Object[]{
                                    event.getRepository(),
                                    push.getCommits().size()
                            }
                    );
                    push.getCommits()
                            .stream()
                            .map(Unchecked.function(pushCommit -> new Tuple2<>(pushCommit, github.getRepository(event.getRepository().getFullName()).getCommit(pushCommit.getSha()))))
                            .filter(Unchecked.predicate(NON_MERGE_COMMITS))
                            .forEach(Unchecked.consumer(tuple2 -> {

                                GHCommit ghCommit = tuple2.v2;
                                logger.debug(
                                        "Commit details [{}]; lines changed {}",
                                        ghCommit.getCommitShortInfo().getMessage(),
                                        ghCommit.getLinesChanged()
                                );
                                ghCommit.getFiles().stream()
                                        .filter(file -> file.getFileName().endsWith(".java"))
                                        .forEach(Unchecked.consumer(file -> handleFile(file, event, tuple2.v1)));

                            }));
                }));
    }

    private void handleFile(GHCommit.File file, GHEventInfo event, GHEventPayload.Push.PushCommit pushCommit) throws IOException, URISyntaxException {
        String patch = file.getPatch();

        if (patch != null) {

            logger.debug(
                    "Repo [{}] " +
                            "File [{}];" +
                            " blob URL [{}] " +
                            "diff length [{}]",
                    event.getRepository().getFullName(),
                    file.getFileName(),
                    file.getBlobUrl(),
                    patch.length()
            );


            Request request = new Request.Builder()
                    .url(file.getRawUrl())
                    .build();

            Response response = okClient.newCall(request).execute();
            List<RuleViolation> ruleViolations = fileAnalyser.runPMD(response.body().byteStream(), file.getFileName());

            long pmdCount = ruleViolations.stream().map(pmdProblem -> {

                try {
                    while (recentCommits.commits.putIfAbsent(
                            System.currentTimeMillis(),
                            new PmdProblem(pmdProblem, convertToHelpfulGithubUrl(file, pmdProblem))
                    ) != null) {
                        // ensure each problem gets put in under a unique timestamp
                        Thread.sleep(1);
                    }
                } catch (InterruptedException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return pmdProblem;
            }).count();


            if (pmdCount > 0) {
                logger.debug("PMDs for patch {}", patch);
            }

            recentCommits.cleanOldCommits();
        }
    }

    private URI convertToHelpfulGithubUrl(GHCommit.File file, RuleViolation pmdProblem) throws URISyntaxException {
        return new URI(file.getBlobUrl().toString() + "#L" + pmdProblem.getBeginLine() + "-L" + pmdProblem.getEndLine());
    }

}
