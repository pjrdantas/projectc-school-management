package br.com.escola.professorservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("professor.persistence.backfill")
public record ProfessorBackfillProperties(
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        int batchSize,
        boolean failOnMismatch) {

    public ProfessorBackfillProperties {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize deve ser maior que zero");
        }
        if (sourceUsername == null) {
            sourceUsername = "";
        }
        if (sourcePassword == null) {
            sourcePassword = "";
        }
    }
}
