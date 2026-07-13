package br.com.escola.identityaccessservice.infra.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity-access.monolith")
public record MonolithIdentityAccessClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {

    public MonolithIdentityAccessClientProperties {
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
