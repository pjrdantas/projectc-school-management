package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.ProfessorCadastroReadPort;
import br.com.escola.bff.application.usecase.ConsultarProfessorUseCase;
import reactor.core.publisher.Mono;

public class ProfessorReadProxyService implements ConsultarProfessorUseCase {

    private final InternalAuthContextPort authContextPort;
    private final ProfessorCadastroReadPort peopleProfessorReadPort;

    public ProfessorReadProxyService(
            InternalAuthContextPort authContextPort,
            ProfessorCadastroReadPort peopleProfessorReadPort) {
        this.authContextPort = authContextPort;
        this.peopleProfessorReadPort = peopleProfessorReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarProfessores(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleProfessorReadPort.listarProfessores(query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> buscarProfessorPorId(
            String authorization,
            String correlationId,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleProfessorReadPort.buscarProfessorPorId(professorId, query, context));
    }
}

