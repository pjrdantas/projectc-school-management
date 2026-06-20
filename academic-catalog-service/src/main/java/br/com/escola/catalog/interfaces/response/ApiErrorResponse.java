package br.com.escola.catalog.interfaces.response;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId) {
}
