package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PedagogicalAvaliacaoPort {

    Mono<ResponseEntity<String>> criar(
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> listar(
            UUID professorTurmaDisciplinaId,
            UUID turmaId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarPorId(
            UUID avaliacaoId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> lancarNota(
            UUID avaliacaoId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> listarNotasPorAvaliacao(
            UUID avaliacaoId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> listarNotasPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query,
            AuthSessionContext context);
}
