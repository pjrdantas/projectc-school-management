package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.TransferenciaMatriculaReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.usecase.ConsultarTransferenciaUseCase;
import reactor.core.publisher.Mono;

public class TransferenciaReadProxyService implements ConsultarTransferenciaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final TransferenciaMatriculaReadPort enrollmentDocumentTransferenciaReadPort;

    public TransferenciaReadProxyService(
            InternalAuthContextPort authContextPort,
            TransferenciaMatriculaReadPort enrollmentDocumentTransferenciaReadPort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentTransferenciaReadPort = enrollmentDocumentTransferenciaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarTransferenciasPorAluno(
            String authorization,
            String correlationId,
            UUID alunoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentTransferenciaReadPort.listarTransferenciasPorAluno(
                        alunoId,
                        query,
                        context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarTransferenciaPorId(
            String authorization,
            String correlationId,
            UUID transferenciaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentTransferenciaReadPort.buscarTransferenciaPorId(
                        transferenciaId,
                        query,
                        context));
    }
}

