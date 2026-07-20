package br.com.escola.institutionaltenantservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("institutional-tenant.persistence.read")
public record LeituraTenantProperties(
        String url,
        String username,
        String password) {
}
