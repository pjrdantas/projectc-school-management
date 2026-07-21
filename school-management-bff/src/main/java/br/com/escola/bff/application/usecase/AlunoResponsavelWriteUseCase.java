package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface AlunoResponsavelWriteUseCase {

    Mono<ResponseEntity<String>> vincular(
            String authorization,
            String correlationId,
            UUID alunoId,
            String requestBody);

    Mono<ResponseEntity<String>> desvincular(
            String authorization,
            String correlationId,
            UUID alunoId,
            UUID responsavelId);
}
