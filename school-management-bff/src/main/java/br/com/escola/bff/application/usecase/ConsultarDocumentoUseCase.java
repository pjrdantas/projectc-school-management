package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarDocumentoUseCase {

    Mono<ResponseEntity<String>> listarDocumentosPorEntidade(
            String authorization,
            String correlationId,
            String entidadeTipo,
            UUID entidadeId);
}
