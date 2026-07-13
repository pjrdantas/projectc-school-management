package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface CriarAulaUseCase {

    Mono<ResponseEntity<String>> criar(
            String authorization,
            String correlationId,
            String requestBody);
}
