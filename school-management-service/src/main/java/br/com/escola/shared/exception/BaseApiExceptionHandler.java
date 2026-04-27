package br.com.escola.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseApiExceptionHandler {

    protected ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            List<ApiFieldError> fields) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                fields);
        return ResponseEntity.status(status).body(response);
    }

    protected ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {
        return buildError(status, error, message, request, List.of());
    }
}
