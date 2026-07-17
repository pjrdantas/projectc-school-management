package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyAvaliacaoReadPort {

    Mono<ResponseEntity<String>> listar(
            UUID professorTurmaDisciplinaId,
            UUID turmaId,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> buscarPorId(
            UUID avaliacaoId,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> listarNotasPorAvaliacao(
            UUID avaliacaoId,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> listarNotasPorMatricula(
            UUID matriculaId,
            CatalogReadQuery query);
}

