package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarProfessorUseCase {

    Mono<ResponseEntity<String>> listarProfessores(String authorization, String correlationId);

    Mono<ResponseEntity<String>> buscarProfessorPorId(String authorization, String correlationId, UUID professorId);
}
