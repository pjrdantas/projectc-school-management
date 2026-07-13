package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PedagogicalAulaPort;
import br.com.escola.bff.application.usecase.ConsultarAulaUseCase;
import br.com.escola.bff.application.usecase.CriarAulaUseCase;
import reactor.core.publisher.Mono;

public class AulaProxyService implements ConsultarAulaUseCase, CriarAulaUseCase {

    private final AuthContextPort authContextPort;
    private final PedagogicalAulaPort pedagogicalAulaPort;

    public AulaProxyService(AuthContextPort authContextPort, PedagogicalAulaPort pedagogicalAulaPort) {
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
    public Mono<ResponseEntity<String>> listar(
            String authorization,
            String correlationId,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.listar(professorTurmaDisciplinaId, turmaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarPorId(String authorization, String correlationId, UUID aulaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.buscarPorId(aulaId, query, context));
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
    public Mono<ResponseEntity<String>> listarFrequenciaProfessor(
            String authorization,
            String correlationId,
            UUID aulaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.listarFrequenciaProfessor(aulaId, query, context));
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

    @Override
    public Mono<ResponseEntity<String>> listarFrequenciasAlunos(
            String authorization,
            String correlationId,
            UUID aulaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> pedagogicalAulaPort.listarFrequenciasAlunos(aulaId, query, context));
    }
}
