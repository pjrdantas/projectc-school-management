package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarProfessorUseCase {

    Mono<ResponseEntity<String>> listarProfessores(String authorization, String correlationId);

    Mono<ResponseEntity<String>> buscarProfessorPorId(String authorization, String correlationId, UUID professorId);

    Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(String authorization, String correlationId, UUID professorId);

    Mono<ResponseEntity<String>> listarProfessoresPorTurma(String authorization, String correlationId, UUID turmaId);

    Mono<ResponseEntity<String>> listarFuncionariosElegiveis(String authorization, String correlationId);
}
