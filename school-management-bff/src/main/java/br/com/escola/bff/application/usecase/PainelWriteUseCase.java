package br.com.escola.bff.application.usecase;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface PainelWriteUseCase {

    Mono<ResponseEntity<String>> encaminhar(
            String authorization, String correlationId, HttpMethod method, String caminhoInterno, String requestBody);
}
