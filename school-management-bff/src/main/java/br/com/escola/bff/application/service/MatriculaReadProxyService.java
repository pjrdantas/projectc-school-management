package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.MatriculaDocumentoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.usecase.ConsultarMatriculaUseCase;
import reactor.core.publisher.Mono;

public class MatriculaReadProxyService implements ConsultarMatriculaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final MatriculaDocumentoReadPort enrollmentDocumentMatriculaReadPort;

    public MatriculaReadProxyService(
            InternalAuthContextPort authContextPort,
            MatriculaDocumentoReadPort enrollmentDocumentMatriculaReadPort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentMatriculaReadPort = enrollmentDocumentMatriculaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarMatriculas(
            String authorization,
            String correlationId,
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentMatriculaReadPort.listarMatriculas(
                        alunoId,
                        turmaId,
                        periodoLetivoId,
                        status,
                        query,
                        context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentMatriculaReadPort.buscarMatricula(matriculaId, query, context));
    }
}

