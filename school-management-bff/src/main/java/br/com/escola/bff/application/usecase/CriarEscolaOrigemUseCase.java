package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface CriarEscolaOrigemUseCase {

    Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            String requestBody);
}
