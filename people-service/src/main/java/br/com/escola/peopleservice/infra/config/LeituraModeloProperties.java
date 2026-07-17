package br.com.escola.peopleservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "people.read-model")
public record LeituraModeloProperties(
        boolean enabled,
        boolean migrationEnabled,
        boolean localReadRoutingEnabled,
        boolean failOnError,
        boolean backfillEnabled,
        boolean reconciliationEnabled,
        int backfillBatchSize,
        boolean fallbackEnabled) {
}


