package br.com.escola.planningaiservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("planning-ai.persistence.backfill")
public record PlanejamentoBackfillProperties(
        String sourceUrl, String sourceUsername, String sourcePassword, int batchSize, boolean failOnMismatch) {
    public PlanejamentoBackfillProperties {
        if (batchSize <= 0) batchSize = 500;
        if (sourceUsername == null) sourceUsername = "";
        if (sourcePassword == null) sourcePassword = "";
    }
}
