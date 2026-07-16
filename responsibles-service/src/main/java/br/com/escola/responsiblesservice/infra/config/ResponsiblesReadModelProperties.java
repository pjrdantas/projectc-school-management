package br.com.escola.responsiblesservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "responsibles.read-model")
public record ResponsiblesReadModelProperties(
        boolean enabled,
        boolean migrationEnabled,
        boolean localReadRoutingEnabled,
        boolean backfillEnabled,
        int backfillBatchSize,
        boolean failOnError,
        boolean fallbackEnabled) {
}
