package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarBoletimUseCase {

    Mono<ResponseEntity<String>> consultarBoletimPorMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId);
}
