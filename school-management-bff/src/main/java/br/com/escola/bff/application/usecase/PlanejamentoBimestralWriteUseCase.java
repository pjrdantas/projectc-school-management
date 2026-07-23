package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface PlanejamentoBimestralWriteUseCase {

    Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody);

    Mono<ResponseEntity<String>> atualizar(
            String authorization,
            String correlationId,
            UUID planejamentoId,
            String requestBody);

    Mono<ResponseEntity<String>> alterarStatus(
            String authorization,
            String correlationId,
            UUID planejamentoId,
            String requestBody);

    Mono<ResponseEntity<String>> adicionarAula(String authorization, String correlationId, UUID planejamentoId, String requestBody);

    Mono<ResponseEntity<String>> adicionarAvaliacao(String authorization, String correlationId, UUID planejamentoId, String requestBody);
}
