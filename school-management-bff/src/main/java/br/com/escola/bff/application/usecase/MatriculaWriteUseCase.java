package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface MatriculaWriteUseCase {

    Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody);

    Mono<ResponseEntity<String>> atualizar(
            String authorization,
            String correlationId,
            UUID matriculaId,
            String requestBody);

    Mono<ResponseEntity<String>> atualizarStatus(
            String authorization,
            String correlationId,
            UUID matriculaId,
            String requestBody);

    Mono<ResponseEntity<String>> cancelar(
            String authorization,
            String correlationId,
            UUID matriculaId,
            String requestBody);
}
