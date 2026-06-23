package br.com.escola.bff.application.service;

public record CatalogWriteCutoverDecision(
        CatalogWriteRoute route,
        boolean useCatalog,
        String reason
) {
}
