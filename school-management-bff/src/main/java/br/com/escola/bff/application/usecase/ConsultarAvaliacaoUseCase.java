package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarAvaliacaoUseCase {

    Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            UUID professorTurmaDisciplinaId,
            UUID turmaId);

    Mono<ResponseEntity<String>> buscarPorId(
            String authorization,
            String correlationId,
            UUID avaliacaoId);

    Mono<ResponseEntity<String>> listarNotasPorAvaliacao(
            String authorization,
            String correlationId,
            UUID avaliacaoId);

    Mono<ResponseEntity<String>> listarNotasPorMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId);
}
