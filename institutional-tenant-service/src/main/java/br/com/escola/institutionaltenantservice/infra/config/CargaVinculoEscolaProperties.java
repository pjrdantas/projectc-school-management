package br.com.escola.institutionaltenantservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("institutional-tenant.persistence.user-school-load")
public record CargaVinculoEscolaProperties(
        boolean enabled,
        String sourceUrl,
        String sourceUsername,
        String sourcePassword,
        String targetUrl,
        String targetUsername,
        String targetPassword,
        String migrationLocations,
        int batchSize,
        boolean failOnMismatch) {

    public CargaVinculoEscolaProperties {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize deve ser maior que zero");
        }
    }
}
