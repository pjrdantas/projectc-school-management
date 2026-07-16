package br.com.escola.responsiblesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ResponsiblesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResponsiblesServiceApplication.class, args);
    }
}
