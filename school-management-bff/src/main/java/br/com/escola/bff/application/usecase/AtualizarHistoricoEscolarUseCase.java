package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface AtualizarHistoricoEscolarUseCase {

    Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            UUID historicoEscolarId,
            String requestBody);
}
