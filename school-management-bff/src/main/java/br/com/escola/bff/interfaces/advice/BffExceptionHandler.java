package br.com.escola.bff.interfaces.advice;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.interfaces.response.ApiErrorResponse;

@RestControllerAdvice
public class BffExceptionHandler {

    @ExceptionHandler(DownstreamRejectedException.class)
    ResponseEntity<ApiErrorResponse> handleRejected(
            DownstreamRejectedException exception,
            ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(exception.status());
        if (status == null || !status.is4xxClientError()) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return ResponseEntity.status(status).body(error(
                status,
                "DOWNSTREAM_REJECTED",
                "A requisicao foi rejeitada pelo servico interno",
                exchange));
    }

    @ExceptionHandler(DownstreamUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleUnavailable(
            DownstreamUnavailableException exception,
            ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "CATALOG_READ_UNAVAILABLE",
                "A leitura de catalogo esta temporariamente indisponivel",
                exchange));
    }

    private ApiErrorResponse error(
            HttpStatus status,
            String code,
            String message,
            ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders()
                .getFirst(TrustedHeaders.CORRELATION_ID);
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                exchange.getRequest().getPath().value(),
                correlationId);
    }
}
