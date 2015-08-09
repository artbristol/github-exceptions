package eu.ocathain.github.exceptions.app;

import eu.ocathain.github.exceptions.github.GithubSlurper;
import eu.ocathain.github.exceptions.http.JsonController;
import eu.ocathain.github.exceptions.pmd.PmdProblem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackageClasses = {GithubSlurper.class, PmdProblem.class, JsonController.class, Main.class})
public class Main extends WebMvcConfigurerAdapter {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
    }
}
