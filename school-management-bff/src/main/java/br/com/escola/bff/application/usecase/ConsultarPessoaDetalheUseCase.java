package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarPessoaDetalheUseCase {

    Mono<ResponseEntity<String>> buscarPessoaPorId(String authorization, String correlationId, UUID pessoaId);

    Mono<ResponseEntity<String>> buscarEnderecoPrincipal(String authorization, String correlationId, UUID pessoaId);

    Mono<ResponseEntity<String>> listarEnderecos(String authorization, String correlationId, UUID pessoaId);

    Mono<ResponseEntity<String>> buscarContato(String authorization, String correlationId, UUID pessoaId);

    Mono<ResponseEntity<String>> listarDocumentos(String authorization, String correlationId, UUID pessoaId);

    Mono<ResponseEntity<String>> buscarDocumentoPorId(String authorization, String correlationId, UUID documentoId);
}
