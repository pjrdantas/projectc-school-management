package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentTransferenciaReadPort;
import br.com.escola.bff.application.usecase.ConsultarTransferenciaUseCase;
import reactor.core.publisher.Mono;

public class TransferenciaReadProxyService implements ConsultarTransferenciaUseCase {

    private final AuthContextPort authContextPort;
    private final EnrollmentDocumentTransferenciaReadPort enrollmentDocumentTransferenciaReadPort;

    public TransferenciaReadProxyService(
            AuthContextPort authContextPort,
            EnrollmentDocumentTransferenciaReadPort enrollmentDocumentTransferenciaReadPort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentTransferenciaReadPort = enrollmentDocumentTransferenciaReadPort;
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
