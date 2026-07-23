package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

import reactor.core.publisher.Mono;

public interface ConsultarAlunoUseCase {

    Mono<ResponseEntity<String>> listar(String authorization, String correlationId, String nome);

    Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID alunoId);

    Mono<ResponseEntity<String>> buscarFicha(String authorization, String correlationId, UUID alunoId);

    Mono<ResponseEntity<String>> encaminharEscrita(
            HttpMethod method, UUID alunoId, String body, String authorization, String correlationId);
}
