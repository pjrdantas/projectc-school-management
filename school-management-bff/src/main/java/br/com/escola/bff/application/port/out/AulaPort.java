package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface AulaPort {

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
            UUID aulaId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> registrarFrequenciaProfessor(
            UUID aulaId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> listarFrequenciaProfessor(
            UUID aulaId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> registrarFrequenciaAluno(
            UUID aulaId,
            String requestBody,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> listarFrequenciasAlunos(
            UUID aulaId,
            CatalogReadQuery query,
            AuthSessionContext context);
}

