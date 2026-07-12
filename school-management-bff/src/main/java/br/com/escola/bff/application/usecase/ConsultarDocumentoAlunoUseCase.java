package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarDocumentoAlunoUseCase {

    Mono<ResponseEntity<String>> listarDocumentosPorAluno(
            String authorization,
            String correlationId,
            UUID alunoId);

    Mono<ResponseEntity<String>> buscarDocumentoAlunoPorId(
            String authorization,
            String correlationId,
            UUID id);
}
