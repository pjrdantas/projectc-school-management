package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface SelecionarEscolaAtivaUseCase {

    Mono<ResponseEntity<String>> selecionarEscolaAtiva(
            String authorization,
            String correlationId,
            String requestBody);
}
