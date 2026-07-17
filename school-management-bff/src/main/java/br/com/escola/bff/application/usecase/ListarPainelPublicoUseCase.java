package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ListarPainelPublicoUseCase {

    Mono<ResponseEntity<String>> listarPublicos(
            String authorization,
            String correlationId);
}

