package br.com.escola.bff.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import br.com.escola.bff.application.service.IdentityTenantRoute;

@ConfigurationProperties("features.identity-tenant-cutover")
public record IdentityTenantCutoverProperties(
        boolean enabled,
        boolean fallbackToMonolithOnError,
        RouteFlags routes
) {

    public boolean routeEnabled(IdentityTenantRoute route) {
        return switch (route) {
            case AUTH_ESCOLAS -> routes.authEscolas();
            case AUTH_ESCOLA_ATIVA -> routes.authEscolaAtiva();
            case AUTH_TENANT_ATIVA -> routes.authTenantAtiva();
        };
    }

    public record RouteFlags(
            boolean authEscolas,
            boolean authEscolaAtiva,
            boolean authTenantAtiva
    ) {}
}
