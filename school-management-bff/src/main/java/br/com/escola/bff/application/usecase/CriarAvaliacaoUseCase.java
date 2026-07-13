package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface CriarAvaliacaoUseCase {

    Mono<ResponseEntity<String>> criar(
            String authorization,
            String correlationId,
            String requestBody);
}
