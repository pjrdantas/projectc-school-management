package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface LegacyAulaReadPort {

    Mono<ResponseEntity<String>> listar(
            UUID professorTurmaDisciplinaId,
            UUID turmaId,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> buscarPorId(
            UUID aulaId,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> listarFrequenciaProfessor(
            UUID aulaId,
            CatalogReadQuery query);

    Mono<ResponseEntity<String>> listarFrequenciasAlunos(
            UUID aulaId,
            CatalogReadQuery query);
}

