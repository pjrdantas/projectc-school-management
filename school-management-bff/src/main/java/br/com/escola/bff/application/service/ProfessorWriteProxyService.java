package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.ProfessorWritePort;
import br.com.escola.bff.application.usecase.ProfessorWriteUseCase;
import reactor.core.publisher.Mono;

public class ProfessorWriteProxyService implements ProfessorWriteUseCase {

    private final AuthContextPort authContextPort;
    private final ProfessorWritePort professorWritePort;

    public ProfessorWriteProxyService(AuthContextPort authContextPort, ProfessorWritePort professorWritePort) {
        this.authContextPort = authContextPort;
        this.professorWritePort = professorWritePort;
    }

    @Override
    public Mono<ResponseEntity<String>> criarProfessor(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorWritePort.criarProfessor(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> criarAlocacao(
            String authorization,
            String correlationId,
            UUID professorId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorWritePort.criarAlocacao(professorId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizarProfessor(
            String authorization,
            String correlationId,
            UUID professorId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorWritePort.atualizarProfessor(professorId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> atualizarAlocacao(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID alocacaoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorWritePort.atualizarAlocacao(
                        professorId, alocacaoId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> encerrarAlocacao(
            String authorization,
            String correlationId,
            UUID professorId,
            UUID alocacaoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorWritePort.encerrarAlocacao(professorId, alocacaoId, query, context));
    }
}
