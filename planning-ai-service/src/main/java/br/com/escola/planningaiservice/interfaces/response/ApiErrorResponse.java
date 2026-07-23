package br.com.escola.planningaiservice.interfaces.response;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {
}
