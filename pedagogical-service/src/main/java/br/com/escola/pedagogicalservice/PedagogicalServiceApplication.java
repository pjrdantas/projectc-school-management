package br.com.escola.pedagogicalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PedagogicalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedagogicalServiceApplication.class, args);
    }
}
