package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface CriarAvaliacaoUseCase {

    Mono<ResponseEntity<String>> criar(
            String authorization,
            String correlationId,
            String requestBody);

    Mono<ResponseEntity<String>> lancarNota(
            String authorization,
            String correlationId,
            UUID avaliacaoId,
            String requestBody);
}
