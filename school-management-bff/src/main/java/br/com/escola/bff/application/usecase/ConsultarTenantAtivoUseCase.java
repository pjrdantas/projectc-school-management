package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarTenantAtivoUseCase {

    Mono<ResponseEntity<String>> consultarTenantAtivo(String authorization, String correlationId);
}
