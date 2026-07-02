package br.com.escola.peopleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PeopleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeopleServiceApplication.class, args);
    }
}
