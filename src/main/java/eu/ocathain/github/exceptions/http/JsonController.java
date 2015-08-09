package eu.ocathain.github.exceptions.http;

import eu.ocathain.github.exceptions.github.RecentCommits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class JsonController {

    @Autowired
    private RecentCommits recentCommits;

    @RequestMapping(value = "/events.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<JsonResponse> home(org.springframework.web.context.request.WebRequest request) {

        if (recentCommits.commits.isEmpty()) {
            return Collections.emptyList();
        }
        if (request.checkNotModified(recentCommits.commits.lastKey())) {
            return null;
        } else {
            return recentCommits.commits.tailMap(System.currentTimeMillis() - RecentCommits.RECENCY_MILLISECONDS)
                    .entrySet().stream().map(entry -> {
                        JsonResponse jsonResponse = new JsonResponse();
                        jsonResponse.setLineNumber(entry.getValue().ruleViolation.getBeginLine());
                        jsonResponse.setDate(new Date(entry.getKey()));
                        jsonResponse.setPmdProblem(entry.getValue().ruleViolation.getRule().getName());
                        jsonResponse.setCommitUrl(entry.getValue().uri);
                        return jsonResponse;
                    }).collect(Collectors.toList());
        }
    }
}
