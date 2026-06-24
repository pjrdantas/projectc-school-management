package br.com.escola.professorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AcademicProfessorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademicProfessorServiceApplication.class, args);
    }
}
