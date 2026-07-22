package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface ProfessorWritePort {

    Mono<ResponseEntity<String>> criarProfessor(
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> criarAlocacao(
            UUID professorId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> atualizarProfessor(
            UUID professorId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> atualizarAlocacao(
            UUID professorId,
            UUID alocacaoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> encerrarAlocacao(
            UUID professorId,
            UUID alocacaoId,
            CatalogReadQuery query,
            AuthSessionContext context);
}
