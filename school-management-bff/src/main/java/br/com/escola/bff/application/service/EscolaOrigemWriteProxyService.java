package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentEscolaOrigemWritePort;
import br.com.escola.bff.application.usecase.CriarEscolaOrigemUseCase;
import reactor.core.publisher.Mono;

public class EscolaOrigemWriteProxyService implements CriarEscolaOrigemUseCase {

    private final AuthContextPort authContextPort;
    private final EnrollmentDocumentEscolaOrigemWritePort enrollmentDocumentEscolaOrigemWritePort;

    public EscolaOrigemWriteProxyService(
            AuthContextPort authContextPort,
            EnrollmentDocumentEscolaOrigemWritePort enrollmentDocumentEscolaOrigemWritePort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentEscolaOrigemWritePort = enrollmentDocumentEscolaOrigemWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> executar(
            String authorization,
            String correlationId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentEscolaOrigemWritePort.criarEscolaOrigem(requestBody, query, context));
    }
}
