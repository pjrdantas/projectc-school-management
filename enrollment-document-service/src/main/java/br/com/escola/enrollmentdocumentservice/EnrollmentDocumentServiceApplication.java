package br.com.escola.enrollmentdocumentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EnrollmentDocumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnrollmentDocumentServiceApplication.class, args);
    }
}
