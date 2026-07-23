package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ProfessorWriteUseCase {

    Mono<ResponseEntity<String>> criarProfessor(String authorization, String correlationId, String requestBody);

    Mono<ResponseEntity<String>> criarAlocacao(
            String authorization,
            String correlationId,
            UUID professorId,
            String requestBody);

    Mono<ResponseEntity<String>> atualizarProfessor(
            String authorization,
            String correlationId,
            UUID professorId,
            String requestBody);

    Mono<ResponseEntity<String>> atualizarAlocacao(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID alocacaoId,
            String requestBody);

    Mono<ResponseEntity<String>> encerrarAlocacao(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID alocacaoId);
}
