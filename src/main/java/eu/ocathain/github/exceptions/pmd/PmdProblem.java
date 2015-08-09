package eu.ocathain.github.exceptions.pmd;

import net.sourceforge.pmd.RuleViolation;

import java.net.URI;

public class PmdProblem {

    public final RuleViolation ruleViolation;
    public final URI uri;

    public PmdProblem(RuleViolation ruleViolation, URI uri) {
        this.ruleViolation = ruleViolation;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "Rule Violation: " + "@ line: " + ruleViolation.getBeginLine() +
                "and @column:" + ruleViolation.getBeginColumn() + " @name: " + ruleViolation.getRule().getName();
    }
}
