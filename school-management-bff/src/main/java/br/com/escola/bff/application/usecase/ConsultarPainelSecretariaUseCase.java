package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarPainelSecretariaUseCase {

    Mono<ResponseEntity<String>> consultar(String authorization, String correlationId);
}

