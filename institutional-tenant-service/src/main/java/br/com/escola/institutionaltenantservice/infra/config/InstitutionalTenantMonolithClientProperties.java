package br.com.escola.institutionaltenantservice.infra.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "institutional-tenant.monolith")
public record InstitutionalTenantMonolithClientProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {

    public InstitutionalTenantMonolithClientProperties {
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(5) : readTimeout;
    }
}
