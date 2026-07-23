package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.DocumentoAlunoMatriculaReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.usecase.ConsultarDocumentoAlunoUseCase;
import reactor.core.publisher.Mono;

public class DocumentoAlunoReadProxyService implements ConsultarDocumentoAlunoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DocumentoAlunoMatriculaReadPort enrollmentDocumentAlunoReadPort;

    public DocumentoAlunoReadProxyService(
            InternalAuthContextPort authContextPort,
            DocumentoAlunoMatriculaReadPort enrollmentDocumentAlunoReadPort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentAlunoReadPort = enrollmentDocumentAlunoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarDocumentosPorAluno(
            String authorization,
            String correlationId,
            UUID alunoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentAlunoReadPort.listarDocumentosPorAluno(alunoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarDocumentoAlunoPorId(
            String authorization,
            String correlationId,
            UUID id) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentAlunoReadPort.buscarDocumentoAlunoPorId(id, query, context));
    }
}

