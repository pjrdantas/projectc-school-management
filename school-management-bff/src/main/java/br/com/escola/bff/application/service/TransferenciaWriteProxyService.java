package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.TransferenciaMatriculaWritePort;
import br.com.escola.bff.application.usecase.CriarTransferenciaUseCase;
import reactor.core.publisher.Mono;

public class TransferenciaWriteProxyService implements CriarTransferenciaUseCase {

    private final AuthContextPort authContextPort;
    private final TransferenciaMatriculaWritePort enrollmentDocumentTransferenciaWritePort;

    public TransferenciaWriteProxyService(
            AuthContextPort authContextPort,
            TransferenciaMatriculaWritePort enrollmentDocumentTransferenciaWritePort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentTransferenciaWritePort = enrollmentDocumentTransferenciaWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentTransferenciaWritePort.criarTransferencia(requestBody, query, context));
    }
}

