package br.com.escola.professorservice.infra.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "professor.shadow.monolith")
public record LegacyClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {

    public LegacyClientProperties {
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

