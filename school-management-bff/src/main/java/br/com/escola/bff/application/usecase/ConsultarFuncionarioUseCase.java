package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarFuncionarioUseCase {

    Mono<ResponseEntity<String>> listarFuncionarios(String authorization, String correlationId);

    Mono<ResponseEntity<String>> buscarFuncionarioPorId(String authorization, String correlationId, UUID funcionarioId);
}
