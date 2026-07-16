package br.com.escola.bff.application.usecase;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ListarDashboardIndicadorSnapshotUseCase {

    Mono<ResponseEntity<String>> listarPorPublicoCodigo(
            String authorization,
            String correlationId,
            String publicoCodigo,
            LocalDate referenciaData);
}
