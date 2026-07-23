package br.com.escola.dashboardqueryservice.interfaces.advice;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceConflictException;
import br.com.escola.dashboardqueryservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.dashboardqueryservice.application.exception.InvalidRequestContextException;
import br.com.escola.dashboardqueryservice.interfaces.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

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

    @ExceptionHandler(PainelQueryServiceResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            PainelQueryServiceResourceNotFoundException exception,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(PainelQueryServiceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            PainelQueryServiceConflictException exception,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", exception.getMessage(), request);
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

