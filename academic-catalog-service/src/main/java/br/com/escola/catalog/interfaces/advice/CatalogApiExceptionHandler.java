package br.com.escola.catalog.interfaces.advice;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.catalog.application.context.InternalHeaders;
import br.com.escola.catalog.application.exception.CatalogResourceNotFoundException;
import br.com.escola.catalog.application.exception.InternalApiUnauthorizedException;
import br.com.escola.catalog.application.exception.InvalidRequestContextException;
import br.com.escola.catalog.interfaces.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class CatalogApiExceptionHandler {

    @ExceptionHandler(CatalogResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(
            CatalogResourceNotFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(InternalApiUnauthorizedException.class)
    ResponseEntity<ApiErrorResponse> handleUnauthorized(
            InternalApiUnauthorizedException exception,
            HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "INTERNAL_UNAUTHORIZED", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidRequestContextException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidContext(
            InvalidRequestContextException exception,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_CONTEXT", exception.getMessage(), request);
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request) {
        String correlationId = request.getHeader(InternalHeaders.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = "unknown";
        }
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), code, message, request.getRequestURI(), correlationId));
    }
}
