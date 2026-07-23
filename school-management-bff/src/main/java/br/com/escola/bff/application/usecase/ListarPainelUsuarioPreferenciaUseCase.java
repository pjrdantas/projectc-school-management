package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ListarPainelUsuarioPreferenciaUseCase {

    Mono<ResponseEntity<String>> listar(String authorization, String correlationId, UUID usuarioId, UUID painelId);
}
