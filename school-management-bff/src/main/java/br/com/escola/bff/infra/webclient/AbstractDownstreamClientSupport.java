package br.com.escola.bff.infra.webclient;

import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import br.com.escola.bff.application.exception.DownstreamRejectedException;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import reactor.core.publisher.Mono;

abstract class AbstractDownstreamClientSupport {

    protected Mono<ResponseEntity<String>> handle(ClientResponse response, String unavailableMessage) {
        HttpStatusCode status = response.statusCode();
        if (status.is4xxClientError()) {
            return Mono.error(new DownstreamRejectedException(status.value()));
        }
        if (status.is5xxServerError()) {
            return Mono.error(new DownstreamUnavailableException(unavailableMessage));
        }
        return response.toEntity(String.class)
                .map(entity -> ResponseEntity.status(entity.getStatusCode())
                        .headers(headers -> copyContentHeaders(entity.getHeaders(), headers))
                        .body(entity.getBody()));
    }

    protected Mono<ResponseEntity<byte[]>> handleBinary(ClientResponse response, String unavailableMessage) {
        HttpStatusCode status = response.statusCode();
        if (status.is4xxClientError()) {
            return Mono.error(new DownstreamRejectedException(status.value()));
        }
        if (status.is5xxServerError()) {
            return Mono.error(new DownstreamUnavailableException(unavailableMessage));
        }
        return response.toEntity(byte[].class)
                .map(entity -> ResponseEntity.status(entity.getStatusCode())
                        .headers(headers -> copyContentHeaders(entity.getHeaders(), headers))
                        .body(entity.getBody()));
    }

    protected Throwable mapTransportError(Throwable error, String unavailableMessage) {
        if (error instanceof TimeoutException || error instanceof WebClientRequestException) {
            return new DownstreamUnavailableException(unavailableMessage, error);
        }
        return error;
    }

    private void copyContentHeaders(HttpHeaders source, HttpHeaders target) {
        if (source.getContentType() != null) {
            target.setContentType(source.getContentType());
        }
        if (source.getContentDisposition() != null) {
            target.setContentDisposition(source.getContentDisposition());
        }
        if (source.getContentLength() >= 0) {
            target.setContentLength(source.getContentLength());
        }
    }
}
