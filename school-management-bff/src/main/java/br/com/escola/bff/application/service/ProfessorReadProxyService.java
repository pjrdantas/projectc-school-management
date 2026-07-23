package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.ProfessorReadPort;
import br.com.escola.bff.application.usecase.ConsultarProfessorUseCase;
import reactor.core.publisher.Mono;

public class ProfessorReadProxyService implements ConsultarProfessorUseCase {

    private final InternalAuthContextPort authContextPort;
    private final ProfessorReadPort professorReadPort;

    public ProfessorReadProxyService(
            InternalAuthContextPort authContextPort,
            ProfessorReadPort professorReadPort) {
        this.authContextPort = authContextPort;
        this.professorReadPort = professorReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarProfessores(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorReadPort.listarProfessores(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarProfessorPorId(
            String authorization,
            String correlationId,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorReadPort.buscarProfessorPorId(professorId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarAlocacoesPorProfessor(
            String authorization,
            String correlationId,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorReadPort.listarAlocacoesPorProfessor(professorId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarProfessoresPorTurma(
            String authorization,
            String correlationId,
            UUID turmaId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorReadPort.listarProfessoresPorTurma(turmaId, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarFuncionariosElegiveis(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> professorReadPort.listarFuncionariosElegiveis(query, context));
    }
}

