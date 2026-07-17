package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelProfessorReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelProfessorUseCase;
import reactor.core.publisher.Mono;

public class PainelProfessorReadProxyService implements ConsultarPainelProfessorUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelProfessorReadPort dashboardProfessorReadPort;

    public PainelProfessorReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelProfessorReadPort dashboardProfessorReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardProfessorReadPort = dashboardProfessorReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId, UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardProfessorReadPort.consultar(professorId, query, context));
    }
}
