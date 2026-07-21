package br.com.escola.enrollmentdocumentservice.infra.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("enrollment-document.matricula-aluno")
public record AlunoMatriculaClientProperties(
        URI baseUrl,
        String internalToken,
        Duration connectTimeout,
        Duration readTimeout) {

    public AlunoMatriculaClientProperties {
        if (baseUrl == null) {
            baseUrl = URI.create("http://localhost:8093");
        }
        if (internalToken == null) {
            internalToken = "";
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(5);
        }
    }
}
