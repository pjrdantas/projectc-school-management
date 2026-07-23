package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarTransferenciaUseCase {

    Mono<ResponseEntity<String>> listarTransferenciasPorAluno(
            String authorization,
            String correlationId,
            UUID alunoId);

    Mono<ResponseEntity<String>> buscarTransferenciaPorId(
            String authorization,
            String correlationId,
            UUID transferenciaId);
}
