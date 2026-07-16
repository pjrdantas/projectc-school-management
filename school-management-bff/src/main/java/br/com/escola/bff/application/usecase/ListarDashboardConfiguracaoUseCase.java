package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ListarDashboardConfiguracaoUseCase {

    Mono<ResponseEntity<String>> listarDashboards(
            String authorization,
            String correlationId,
            UUID publicoDashboardId,
            String publicoCodigo);
}
