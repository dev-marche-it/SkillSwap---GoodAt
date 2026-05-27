package it.skillswap.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point server web: espone API REST e interfaccia statica su {@code /}.
 */
@SpringBootApplication(scanBasePackages = "it.skillswap")
public class SkillSwapWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillSwapWebApplication.class, args);
    }
}
