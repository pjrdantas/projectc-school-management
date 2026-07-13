package br.com.escola.pedagogicalservice.interfaces.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.pedagogicalservice.application.exception.DownstreamUnavailableException;
import br.com.escola.pedagogicalservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.pedagogicalservice.application.exception.InvalidRequestContextException;
import br.com.escola.pedagogicalservice.application.exception.PedagogicalServiceResourceNotFoundException;
import br.com.escola.pedagogicalservice.interfaces.response.ApiErrorResponse;

@RestControllerAdvice
public class PedagogicalServiceApiExceptionHandler {

    @ExceptionHandler(InternalApiUnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(InternalApiUnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse("INTERNAL_UNAUTHORIZED", exception.getMessage()));
    }

    @ExceptionHandler(InvalidRequestContextException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidContext(InvalidRequestContextException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("INVALID_REQUEST_CONTEXT", exception.getMessage()));
    }

    @ExceptionHandler(PedagogicalServiceResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(PedagogicalServiceResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("RESOURCE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DownstreamUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnavailable(DownstreamUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorResponse("DOWNSTREAM_UNAVAILABLE", exception.getMessage()));
    }
}
