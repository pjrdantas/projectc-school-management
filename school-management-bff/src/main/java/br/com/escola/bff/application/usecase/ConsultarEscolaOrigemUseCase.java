package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarEscolaOrigemUseCase {

    Mono<ResponseEntity<String>> buscarEscolaOrigemPorId(
            String authorization,
            String correlationId,
            UUID escolaOrigemId);
}
