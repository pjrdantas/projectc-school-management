package br.com.escola.identityaccessservice.infra.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("identity-access.school-session")
public record EscolaSessaoClientProperties(
        URI baseUrl,
        String internalToken,
        Duration connectTimeout,
        Duration readTimeout) {

    public EscolaSessaoClientProperties {
        if (baseUrl == null) {
            baseUrl = URI.create("http://localhost:8092");
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
