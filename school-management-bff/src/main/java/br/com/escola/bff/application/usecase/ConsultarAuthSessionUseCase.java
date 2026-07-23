package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarAuthSessionUseCase {

    Mono<ResponseEntity<String>> listarEscolas(String authorization, String correlationId);
}
