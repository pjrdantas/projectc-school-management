package br.com.escola.pedagogicalservice.interfaces.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.pedagogicalservice.application.exception.InternalApiUnauthorizedException;
import br.com.escola.pedagogicalservice.application.exception.ConflitoNegocioException;
import br.com.escola.pedagogicalservice.application.exception.InvalidRequestContextException;
import br.com.escola.pedagogicalservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.pedagogicalservice.interfaces.response.ApiErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ConflitoNegocioException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflitoNegocioException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("BUSINESS_CONFLICT", exception.getMessage()));
    }

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

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(RecursoNaoEncontradoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("RESOURCE_NOT_FOUND", exception.getMessage()));
    }
}

