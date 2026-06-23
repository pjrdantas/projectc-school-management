package br.com.escola.bff.application.service;

public record CatalogReadCutoverDecision(
        CatalogReadRoute route,
        boolean useCatalog,
        String reason
) {}
