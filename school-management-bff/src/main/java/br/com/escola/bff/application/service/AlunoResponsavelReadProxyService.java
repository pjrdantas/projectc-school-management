package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PeopleAlunoResponsavelReadPort;
import br.com.escola.bff.application.usecase.ConsultarAlunoResponsavelUseCase;
import reactor.core.publisher.Mono;

public class AlunoResponsavelReadProxyService implements ConsultarAlunoResponsavelUseCase {

    private final AuthContextPort authContextPort;
    private final PeopleAlunoResponsavelReadPort peopleAlunoResponsavelReadPort;

    public AlunoResponsavelReadProxyService(
            AuthContextPort authContextPort,
            PeopleAlunoResponsavelReadPort peopleAlunoResponsavelReadPort) {
        this.authContextPort = authContextPort;
        this.peopleAlunoResponsavelReadPort = peopleAlunoResponsavelReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarResponsaveisPorAluno(
            String authorization,
            String correlationId,
            UUID alunoId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleAlunoResponsavelReadPort.listarResponsaveisPorAluno(alunoId, query, context));
    }
}
