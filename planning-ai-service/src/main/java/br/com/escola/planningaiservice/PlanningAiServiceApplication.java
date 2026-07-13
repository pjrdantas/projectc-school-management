package br.com.escola.planningaiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PlanningAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanningAiServiceApplication.class, args);
    }
}
