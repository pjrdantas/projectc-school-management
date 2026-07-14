package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.EnrollmentDocumentEscolaOrigemReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.usecase.ConsultarEscolaOrigemUseCase;
import reactor.core.publisher.Mono;

public class EscolaOrigemReadProxyService implements ConsultarEscolaOrigemUseCase {

    private final InternalAuthContextPort authContextPort;
    private final EnrollmentDocumentEscolaOrigemReadPort enrollmentDocumentEscolaOrigemReadPort;

    public EscolaOrigemReadProxyService(
            InternalAuthContextPort authContextPort,
            EnrollmentDocumentEscolaOrigemReadPort enrollmentDocumentEscolaOrigemReadPort) {
        this.authContextPort = authContextPort;
        this.enrollmentDocumentEscolaOrigemReadPort = enrollmentDocumentEscolaOrigemReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarEscolasOrigem(
            String authorization,
            String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentEscolaOrigemReadPort.listarEscolasOrigem(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarEscolaOrigemPorId(
            String authorization,
            String correlationId,
            UUID escolaOrigemId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> enrollmentDocumentEscolaOrigemReadPort.buscarEscolaOrigemPorId(
                        escolaOrigemId,
                        query,
                        context));
    }
}
