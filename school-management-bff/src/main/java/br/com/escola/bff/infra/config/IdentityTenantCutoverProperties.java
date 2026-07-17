package br.com.escola.bff.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import br.com.escola.bff.application.service.IdentityTenantRoute;

@ConfigurationProperties("features.identity-tenant-cutover")
public record IdentityTenantCutoverProperties(
        boolean enabled,
        boolean fallbackToLegacyOnError,
        RouteFlags routes
) {

    public boolean routeEnabled(IdentityTenantRoute route) {
        return switch (route) {
            case AUTH_ESCOLAS -> routes.authEscolas();
            case AUTH_ESCOLA_ATIVA -> routes.authEscolaAtiva();
            case AUTH_TENANT_ATIVA -> true;
        };
    }

    public record RouteFlags(
            boolean authEscolas,
            boolean authEscolaAtiva
    ) {}
}

