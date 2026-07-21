package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AlunoResponsavelWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.usecase.AlunoResponsavelWriteUseCase;
import reactor.core.publisher.Mono;

public class AlunoResponsavelWriteProxyService implements AlunoResponsavelWriteUseCase {

    private final AuthContextPort authContextPort;
    private final AlunoResponsavelWritePort alunoResponsavelWritePort;

    public AlunoResponsavelWriteProxyService(
            AuthContextPort authContextPort,
            AlunoResponsavelWritePort alunoResponsavelWritePort) {
        this.authContextPort = authContextPort;
        this.alunoResponsavelWritePort = alunoResponsavelWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> vincular(
            String authorization,
            String correlationId,
            UUID alunoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> alunoResponsavelWritePort.vincular(alunoId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> desvincular(
            String authorization,
            String correlationId,
            UUID alunoId,
            UUID responsavelId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> alunoResponsavelWritePort.desvincular(alunoId, responsavelId, query, context));
    }
}
