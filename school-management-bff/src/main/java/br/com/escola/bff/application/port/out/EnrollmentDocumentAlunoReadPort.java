package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface EnrollmentDocumentAlunoReadPort {

    Mono<ResponseEntity<String>> listarDocumentosPorAluno(
            UUID alunoId,
            CatalogReadQuery query,
            AuthSessionContext context);

    Mono<ResponseEntity<String>> buscarDocumentoAlunoPorId(
            UUID id,
            CatalogReadQuery query,
            AuthSessionContext context);
}
