package br.com.escola.identityaccessservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IdentityAccessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityAccessServiceApplication.class, args);
    }
}
