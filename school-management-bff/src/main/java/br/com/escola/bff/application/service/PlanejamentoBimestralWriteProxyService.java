package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PlanejamentoBimestralWritePort;
import br.com.escola.bff.application.usecase.PlanejamentoBimestralWriteUseCase;
import reactor.core.publisher.Mono;

public class PlanejamentoBimestralWriteProxyService implements PlanejamentoBimestralWriteUseCase {

    private final AuthContextPort authContextPort;
    private final PlanejamentoBimestralWritePort planejamentoBimestralWritePort;

    public PlanejamentoBimestralWriteProxyService(
            AuthContextPort authContextPort,
            PlanejamentoBimestralWritePort planejamentoBimestralWritePort) {
        this.authContextPort = authContextPort;
        this.planejamentoBimestralWritePort = planejamentoBimestralWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planejamentoBimestralWritePort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizar(
            String authorization,
            String correlationId,
            UUID planejamentoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planejamentoBimestralWritePort.atualizar(
                        planejamentoId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> alterarStatus(
            String authorization,
            String correlationId,
            UUID planejamentoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planejamentoBimestralWritePort.alterarStatus(
                        planejamentoId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> adicionarAula(String authorization, String correlationId, UUID planejamentoId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planejamentoBimestralWritePort.adicionarAula(planejamentoId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> adicionarAvaliacao(String authorization, String correlationId, UUID planejamentoId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> planejamentoBimestralWritePort.adicionarAvaliacao(planejamentoId, requestBody, query, context));
    }
}
