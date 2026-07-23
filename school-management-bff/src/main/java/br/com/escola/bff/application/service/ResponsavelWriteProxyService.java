package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.ResponsavelWritePort;
import br.com.escola.bff.application.usecase.ResponsavelWriteUseCase;
import reactor.core.publisher.Mono;

public class ResponsavelWriteProxyService implements ResponsavelWriteUseCase {

    private final AuthContextPort authContextPort;
    private final ResponsavelWritePort responsavelWritePort;

    public ResponsavelWriteProxyService(AuthContextPort authContextPort, ResponsavelWritePort responsavelWritePort) {
        this.authContextPort = authContextPort;
        this.responsavelWritePort = responsavelWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsavelWritePort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizar(
            String authorization,
            String correlationId,
            UUID responsavelId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsavelWritePort.atualizar(responsavelId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> excluir(String authorization, String correlationId, UUID responsavelId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> responsavelWritePort.excluir(responsavelId, query, context));
    }
}
