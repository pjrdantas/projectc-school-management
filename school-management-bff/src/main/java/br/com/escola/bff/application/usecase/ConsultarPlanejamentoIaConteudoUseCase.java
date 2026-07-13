package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarPlanejamentoIaConteudoUseCase {

    Mono<ResponseEntity<String>> listarConteudos(
            String authorization,
            String correlationId,
            UUID planejamentoId);
}
