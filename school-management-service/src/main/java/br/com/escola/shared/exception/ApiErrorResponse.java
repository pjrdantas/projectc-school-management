package br.com.escola.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<ApiFieldError> fields
) {
}
