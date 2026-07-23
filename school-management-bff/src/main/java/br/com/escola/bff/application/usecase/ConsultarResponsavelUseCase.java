package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarResponsavelUseCase {

    Mono<ResponseEntity<String>> listarResponsaveis(
            String authorization,
            String correlationId,
            String nome,
            String cpf);

    Mono<ResponseEntity<String>> buscarResponsavelPorId(
            String authorization,
            String correlationId,
            UUID responsavelId);
}
