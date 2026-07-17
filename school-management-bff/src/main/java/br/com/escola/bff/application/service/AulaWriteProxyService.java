package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.AulaPort;
import br.com.escola.bff.application.usecase.CriarAulaUseCase;
import reactor.core.publisher.Mono;

public class AulaWriteProxyService implements CriarAulaUseCase {

    private final AuthContextPort authContextPort;
    private final AulaPort pedagogicalAulaPort;

    public AulaWriteProxyService(
            AuthContextPort authContextPort,
            AulaPort pedagogicalAulaPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalAulaPort = pedagogicalAulaPort;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> registrarFrequenciaProfessor(
            String authorization,
            String correlationId,
            UUID aulaId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.registrarFrequenciaProfessor(aulaId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> registrarFrequenciaAluno(
            String authorization,
            String correlationId,
            UUID aulaId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.registrarFrequenciaAluno(aulaId, requestBody, query, context));
    }
}

