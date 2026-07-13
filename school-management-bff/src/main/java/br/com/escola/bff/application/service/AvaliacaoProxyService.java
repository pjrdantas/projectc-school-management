package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PedagogicalAvaliacaoPort;
import br.com.escola.bff.application.usecase.ConsultarAvaliacaoUseCase;
import br.com.escola.bff.application.usecase.CriarAvaliacaoUseCase;
import reactor.core.publisher.Mono;

public class AvaliacaoProxyService implements ConsultarAvaliacaoUseCase, CriarAvaliacaoUseCase {

    private final AuthContextPort authContextPort;
    private final PedagogicalAvaliacaoPort pedagogicalAvaliacaoPort;

    public AvaliacaoProxyService(
            AuthContextPort authContextPort,
            PedagogicalAvaliacaoPort pedagogicalAvaliacaoPort) {
        this.authContextPort = authContextPort;
        this.pedagogicalAvaliacaoPort = pedagogicalAvaliacaoPort;
    }

    @Override
    public Mono<ResponseEntity<String>> criar(String authorization, String correlationId, String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.criar(requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.listar(professorTurmaDisciplinaId, turmaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID avaliacaoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.buscarPorId(avaliacaoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> lancarNota(
            String authorization,
            String correlationId,
            UUID avaliacaoId,
            String requestBody) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.lancarNota(avaliacaoId, requestBody, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarNotasPorAvaliacao(
            String authorization,
            String correlationId,
            UUID avaliacaoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.listarNotasPorAvaliacao(avaliacaoId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarNotasPorMatricula(
            String authorization,
            String correlationId,
            UUID matriculaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAvaliacaoPort.listarNotasPorMatricula(matriculaId, query, context));
    }
}
