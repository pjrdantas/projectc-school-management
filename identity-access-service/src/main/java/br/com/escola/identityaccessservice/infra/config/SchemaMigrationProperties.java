package br.com.escola.identityaccessservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("identity-access.persistence.schema-migration")
public record SchemaMigrationProperties(
        boolean enabled,
        String url,
        String username,
        String password,
        String locations) {
}
