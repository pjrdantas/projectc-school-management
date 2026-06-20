package br.com.escola.bff.infra.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("clients.monolith")
public record MonolithClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration responseTimeout) {
}

