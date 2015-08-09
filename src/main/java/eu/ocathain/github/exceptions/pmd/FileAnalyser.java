package eu.ocathain.github.exceptions.pmd;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FileAnalyser {
    private static Logger logger = LoggerFactory.getLogger(FileAnalyser.class);

    private final RuleSets ruleSets = new RuleSets();

    @PostConstruct
    public void setup() {
        Arrays.asList(
                "rulesets/java/android.xml",
                "rulesets/java/basic.xml",
                "rulesets/java/braces.xml",
                "rulesets/java/clone.xml",
                "rulesets/java/codesize.xml",
                "rulesets/java/comments.xml",
                "rulesets/java/controversial.xml",
                "rulesets/java/coupling.xml",
                "rulesets/java/design.xml",
                "rulesets/java/empty.xml",
                "rulesets/java/finalizers.xml",
                "rulesets/java/imports.xml",
                "rulesets/java/j2ee.xml",
                "rulesets/java/javabeans.xml",
                "rulesets/java/junit.xml",
                "rulesets/java/logging-jakarta-commons.xml",
                "rulesets/java/logging-java.xml",
                "rulesets/java/migrating.xml",
                "rulesets/java/naming.xml",
                "rulesets/java/optimizations.xml",
                "rulesets/java/strictexception.xml",
                "rulesets/java/strings.xml",
                "rulesets/java/sunsecure.xml",
                "rulesets/java/typeresolution.xml",
                "rulesets/java/unnecessary.xml",
                "rulesets/java/unusedcode.xml"
        ).stream().map(ruleSetString -> {
            try {
                return new RuleSetFactory().createRuleSet(ruleSetString);
            } catch (RuleSetNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).forEach(ruleSets::addRuleSet);
    }

    public List<RuleViolation> runPMD(InputStream fis, String filename) {
        List<RuleViolation> ruleViolationMsgs = new ArrayList<>();
        try {

            final PMD pmd = new PMD();
            final RuleContext ctx = new RuleContext();
            ctx.setReport(new Report());
            ctx.setSourceCodeFilename(filename);

            pmd.getSourceCodeProcessor().processSourceCode(fis, ruleSets, ctx);

            if (!ctx.getReport().isEmpty()) {
                for (final RuleViolation viol : ctx.getReport()) {
                    ruleViolationMsgs.add(viol);
                }
            }
        } catch (PMDException e) {
            throw new RuntimeException(e);
        }

        logger.debug("PMD violations: {}", ruleViolationMsgs);

        return ruleViolationMsgs;

    }

}