package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface SalvarDiarioClasseUseCase {

    Mono<ResponseEntity<String>> salvar(
            String authorization,
            String correlationId,
            String idDiarioClasse,
            String requestBody);
}
