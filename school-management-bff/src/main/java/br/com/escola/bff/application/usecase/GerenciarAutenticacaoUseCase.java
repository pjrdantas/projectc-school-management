package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface GerenciarAutenticacaoUseCase {

    Mono<ResponseEntity<String>> login(String requestBody, String correlationId);

    Mono<ResponseEntity<String>> refresh(String requestBody, String correlationId);

    Mono<ResponseEntity<String>> logout(String requestBody, String correlationId);
}
