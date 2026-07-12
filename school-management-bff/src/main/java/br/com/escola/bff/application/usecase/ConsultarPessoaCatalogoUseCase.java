package br.com.escola.bff.application.usecase;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarPessoaCatalogoUseCase {

    Mono<ResponseEntity<String>> listarTiposPessoa(String authorization, String correlationId);

    Mono<ResponseEntity<String>> listarTiposEndereco(String authorization, String correlationId);

    Mono<ResponseEntity<String>> listarStatusAluno(String authorization, String correlationId);

    Mono<ResponseEntity<String>> listarParentescos(String authorization, String correlationId);
}
