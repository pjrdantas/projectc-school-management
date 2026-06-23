package br.com.escola.bff.application.dto;

public record CatalogWriteQuery(
        String authorization,
        String correlationId,
        String idempotencyKey
) {
}
