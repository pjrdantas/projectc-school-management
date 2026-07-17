package br.com.escola.enrollmentdocumentservice.infra.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "enrollment-document.monolith")
public record OrigemAtualClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {

    public OrigemAtualClientProperties {
        if (baseUrl == null) {
            baseUrl = URI.create("http://localhost:8080");
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(5);
        }
    }
}

