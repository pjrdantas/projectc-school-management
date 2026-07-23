package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ListarPainelConfiguracaoUseCase {

    Mono<ResponseEntity<String>> listarPainels(
            String authorization,
            String correlationId,
            UUID publicoPainelId,
            String publicoCodigo);

    Mono<ResponseEntity<String>> listarWidgets(String authorization, String correlationId, UUID painelId);
}

