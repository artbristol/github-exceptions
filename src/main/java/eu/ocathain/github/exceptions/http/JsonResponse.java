package eu.ocathain.github.exceptions.http;

import java.net.URI;
import java.util.Date;

public class JsonResponse {

    private String pmdProblem;
    private Date date;
    private URI commitUrl;
    private Integer lineNumber;

    public String getPmdProblem() {
        return pmdProblem;
    }

    public void setPmdProblem(String pmdProblem) {
        this.pmdProblem = pmdProblem;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public URI getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(URI commitUrl) {
        this.commitUrl = commitUrl;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
}
