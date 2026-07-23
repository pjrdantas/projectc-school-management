package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarHistoricoEscolarUseCase {

    Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            Integer page,
            Integer size);

    Mono<ResponseEntity<String>> listarPorAluno(
            String authorization,
            String correlationId,
            UUID alunoId);

    Mono<ResponseEntity<String>> carregarNovo(
            String authorization,
            String correlationId,
            UUID alunoId,
            UUID matriculaId,
            String modo);

    Mono<ResponseEntity<String>> carregarParaEdicao(
            String authorization,
            String correlationId,
            UUID historicoEscolarId);
}
