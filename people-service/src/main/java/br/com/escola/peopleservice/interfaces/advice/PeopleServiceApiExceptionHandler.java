package br.com.escola.peopleservice.interfaces.advice;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.exception.DownstreamUnavailableException;
import br.com.escola.peopleservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.peopleservice.application.exception.InvalidRequestContextException;
import br.com.escola.peopleservice.application.exception.PeopleServiceResourceNotFoundException;
import br.com.escola.peopleservice.interfaces.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class PeopleServiceApiExceptionHandler {

    @ExceptionHandler(PeopleServiceResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(
            PeopleServiceResourceNotFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(InternalApiUnauthorizedException.class)
    ResponseEntity<ApiErrorResponse> handleUnauthorized(
            InternalApiUnauthorizedException exception,
            HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "INTERNAL_UNAUTHORIZED", exception.getMessage(), request);
    }

    @ExceptionHandler({ InvalidRequestContextException.class, MissingRequestHeaderException.class, IllegalArgumentException.class })
    ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
    }

    @ExceptionHandler(DownstreamUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleUnavailable(
            DownstreamUnavailableException exception,
            HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "DOWNSTREAM_UNAVAILABLE", exception.getMessage(), request);
    }

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<ApiErrorResponse> handleDownstreamRejected(
            RestClientResponseException exception,
            HttpServletRequest request) {
        HttpStatus status = exception.getStatusCode().is4xxClientError()
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.SERVICE_UNAVAILABLE;
        String code = exception.getStatusCode().is4xxClientError() ? "DOWNSTREAM_REJECTED" : "DOWNSTREAM_UNAVAILABLE";
        return response(status, code, "Monolito rejeitou a operacao shadow de pessoas", request);
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
