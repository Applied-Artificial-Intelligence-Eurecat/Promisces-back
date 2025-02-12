package org.eurecat.promisces;

import org.eurecat.promisces.solutions.SolutionRepository;
import org.eurecat.promisces.solutions.criteria.SolutionCriteriaRepository;
import org.eurecat.promisces.substances.SubstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PromiscesBackendApplication {

    @Autowired
    private SubstanceRepository substanceRepository;
    @Autowired
    private SolutionRepository solutionRepository;
    @Autowired
    private SolutionCriteriaRepository solutionCriteriaRepository;

    public static void main(String[] args) {
        System.out.println("CLASSPATH: " + System.getProperty("java.class.path"));
        SpringApplication.run(PromiscesBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner lineRunner() {
        return args -> {

        };
    }
}
