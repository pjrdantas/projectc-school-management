package br.com.escola.bff.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import br.com.escola.bff.application.service.CatalogWriteRoute;

@ConfigurationProperties("features.catalog-write-cutover")
public record CatalogWriteCutoverProperties(
        boolean enabled,
        RouteFlags routes
) {

    public boolean routeEnabled(CatalogWriteRoute route) {
        return switch (route) {
            case PERIODOS_LETIVOS -> routes.periodosLetivos();
            case DISCIPLINAS -> routes.disciplinas();
        };
    }

    public record RouteFlags(
            boolean periodosLetivos,
            boolean disciplinas
    ) {}
}
