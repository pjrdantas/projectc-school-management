package br.com.escola.peopleservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "people.persistence")
public record RuntimeProperties(
        boolean enabled,
        boolean localReadRoutingEnabled,
        boolean fallbackEnabled) {
}


