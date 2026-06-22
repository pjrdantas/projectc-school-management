package br.com.escola.bff.application.dto;

public record CatalogReadQuery(
        String authorization,
        String correlationId
) {}
