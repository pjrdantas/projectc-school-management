package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface CriarAulaUseCase {

    Mono<ResponseEntity<String>> criar(
            String authorization,
            String correlationId,
            String requestBody);

    Mono<ResponseEntity<String>> registrarFrequenciaProfessor(
            String authorization,
            String correlationId,
            UUID aulaId,
            String requestBody);

    Mono<ResponseEntity<String>> registrarFrequenciaAluno(
            String authorization,
            String correlationId,
            UUID aulaId,
            String requestBody);
}
