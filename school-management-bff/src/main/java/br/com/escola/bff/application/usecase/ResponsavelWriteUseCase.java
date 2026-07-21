package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ResponsavelWriteUseCase {

    Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody);

    Mono<ResponseEntity<String>> atualizar(
            String authorization,
            String correlationId,
            UUID responsavelId,
            String requestBody);

    Mono<ResponseEntity<String>> excluir(String authorization, String correlationId, UUID responsavelId);
}
