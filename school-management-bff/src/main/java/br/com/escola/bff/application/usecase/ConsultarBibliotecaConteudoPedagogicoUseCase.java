package br.com.escola.bff.application.usecase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

public interface ConsultarBibliotecaConteudoPedagogicoUseCase {

    Mono<ResponseEntity<String>> listarBiblioteca(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema);
}
