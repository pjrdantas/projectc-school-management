package br.com.escola.professorservice.infra.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "professor.catalog-service")
public record CatalogoApoioClientProperties(
        URI baseUrl,
        String internalToken,
        Duration connectTimeout,
        Duration readTimeout) {

    public CatalogoApoioClientProperties {
        if (baseUrl == null) {
            baseUrl = URI.create("http://localhost:8091");
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(5);
        }
    }
}
