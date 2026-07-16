package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarDashboardProfessorUseCase {

    Mono<ResponseEntity<String>> consultar(String authorization, String correlationId, UUID professorId);
}
