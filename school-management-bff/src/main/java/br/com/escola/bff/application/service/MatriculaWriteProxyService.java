package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.MatriculaWritePort;
import br.com.escola.bff.application.usecase.MatriculaWriteUseCase;
import reactor.core.publisher.Mono;

public class MatriculaWriteProxyService implements MatriculaWriteUseCase {

    private final AuthContextPort authContextPort;
    private final MatriculaWritePort matriculaWritePort;

    public MatriculaWriteProxyService(AuthContextPort authContextPort, MatriculaWritePort matriculaWritePort) {
        this.authContextPort = authContextPort;
        this.matriculaWritePort = matriculaWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> matriculaWritePort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizar(
            String authorization,
            String correlationId,
            UUID matriculaId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> matriculaWritePort.atualizar(matriculaId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizarStatus(
            String authorization,
            String correlationId,
            UUID matriculaId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> matriculaWritePort.atualizarStatus(matriculaId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> cancelar(
            String authorization,
            String correlationId,
            UUID matriculaId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> matriculaWritePort.cancelar(matriculaId, requestBody, query, context));
    }
}
