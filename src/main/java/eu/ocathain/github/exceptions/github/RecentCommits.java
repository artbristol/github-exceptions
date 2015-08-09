package eu.ocathain.github.exceptions.github;

import eu.ocathain.github.exceptions.pmd.PmdProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class RecentCommits {

    public static final int RECENCY_MILLISECONDS = 60000;

    private static final Logger logger = LoggerFactory.getLogger(RecentCommits.class);

    public final NavigableMap<Long, PmdProblem> commits = new ConcurrentSkipListMap<>();

    public void cleanOldCommits() {
        NavigableMap<Long, PmdProblem> expiredCommits = commits.headMap(System.currentTimeMillis() - RECENCY_MILLISECONDS, true);

        // leave at least one commit in the map
        while (!expiredCommits.isEmpty() && commits.size() > 1) {
            Map.Entry<Long, PmdProblem> removedCommit = expiredCommits.pollFirstEntry();
            logger.info("Clearing old commit {}", removedCommit == null ? "error: null" : removedCommit.getKey());
        }
    }
}
