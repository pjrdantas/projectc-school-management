package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarAulaUseCase {

    Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            UUID professorTurmaDisciplinaId,
            UUID turmaId);

    Mono<ResponseEntity<String>> buscarPorId(
            String authorization,
            String correlationId,
            UUID aulaId);

    Mono<ResponseEntity<String>> listarFrequenciaProfessor(
            String authorization,
            String correlationId,
            UUID aulaId);

    Mono<ResponseEntity<String>> listarFrequenciasAlunos(
            String authorization,
            String correlationId,
            UUID aulaId);
}
