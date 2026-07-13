package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarMatriculaUseCase {

    Mono<ResponseEntity<String>> listarMatriculas(
            String authorization,
            String correlationId,
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status);
}
