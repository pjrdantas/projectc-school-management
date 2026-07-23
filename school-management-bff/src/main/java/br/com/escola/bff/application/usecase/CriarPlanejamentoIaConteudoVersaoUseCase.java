package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface CriarPlanejamentoIaConteudoVersaoUseCase {

    Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            UUID conteudoId,
            String requestBody);
}
