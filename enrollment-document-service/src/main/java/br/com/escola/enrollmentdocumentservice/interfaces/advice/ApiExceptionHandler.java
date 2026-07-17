package br.com.escola.enrollmentdocumentservice.interfaces.advice;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.enrollmentdocumentservice.application.context.InternalHeaders;
import br.com.escola.enrollmentdocumentservice.application.exception.ConflitoNegocioException;
import br.com.escola.enrollmentdocumentservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.enrollmentdocumentservice.application.exception.InvalidRequestContextException;
import br.com.escola.enrollmentdocumentservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.enrollmentdocumentservice.interfaces.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(InternalApiUnauthorizedException.class)
    ResponseEntity<ApiErrorResponse> handleUnauthorized(
            InternalApiUnauthorizedException exception,
            HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "INTERNAL_UNAUTHORIZED", exception.getMessage(), request);
    }

    @ExceptionHandler({InvalidRequestContextException.class, MissingRequestHeaderException.class, IllegalArgumentException.class})
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
    }

    @ExceptionHandler(ConflitoNegocioException.class)
    ResponseEntity<ApiErrorResponse> handleBusinessConflict(
            ConflitoNegocioException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", exception.getMessage(), request);
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
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                correlationId));
    }
}

