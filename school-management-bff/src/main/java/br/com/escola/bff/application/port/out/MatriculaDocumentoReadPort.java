package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface MatriculaDocumentoReadPort {

    Mono<ResponseEntity<String>> listarStatus(CatalogReadQuery query, AuthSessionContext context);

    Mono<ResponseEntity<String>> listarMatriculas(
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarMatricula(
            UUID matriculaId,
            CatalogReadQuery query,
            AuthSessionContext context);
}

