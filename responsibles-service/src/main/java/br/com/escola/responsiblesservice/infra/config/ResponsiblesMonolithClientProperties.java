package br.com.escola.responsiblesservice.infra.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("responsibles.monolith")
public record ResponsiblesMonolithClientProperties(URI baseUrl) {
}
