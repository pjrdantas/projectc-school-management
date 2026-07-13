package br.com.escola.identityaccessservice.interfaces.advice;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.identityaccessservice.application.exception.DownstreamUnavailableException;
import br.com.escola.identityaccessservice.application.exception.IdentityAccessServiceResourceNotFoundException;
import br.com.escola.identityaccessservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.identityaccessservice.application.exception.InvalidRequestContextException;
import br.com.escola.identityaccessservice.interfaces.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class IdentityAccessServiceApiExceptionHandler {

    @ExceptionHandler(InternalApiUnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            InternalApiUnauthorizedException exception,
            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INTERNAL_UNAUTHORIZED", exception.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidRequestContextException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            Exception exception,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
    }

    @ExceptionHandler(IdentityAccessServiceResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            IdentityAccessServiceResourceNotFoundException exception,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(DownstreamUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnavailable(
            DownstreamUnavailableException exception,
            HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "DOWNSTREAM_UNAVAILABLE", exception.getMessage(), request);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleRestClient(
            RestClientResponseException exception,
            HttpServletRequest request) {
        return build(
                HttpStatus.valueOf(exception.getStatusCode().value()),
                "DOWNSTREAM_HTTP_ERROR",
                exception.getResponseBodyAsString().isBlank() ? exception.getMessage() : exception.getResponseBodyAsString(),
                request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()));
    }
}
