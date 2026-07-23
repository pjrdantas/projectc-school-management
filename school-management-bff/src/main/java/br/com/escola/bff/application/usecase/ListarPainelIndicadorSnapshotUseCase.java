package br.com.escola.bff.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ListarPainelIndicadorSnapshotUseCase {

    Mono<ResponseEntity<String>> listarPorPublicoCodigo(
            String authorization,
            String correlationId,
            String publicoCodigo,
            LocalDate referenciaData);

    Mono<ResponseEntity<String>> listarPorPublicoId(
            String authorization,
            String correlationId,
            UUID publicoId,
            LocalDate referenciaData);
}

