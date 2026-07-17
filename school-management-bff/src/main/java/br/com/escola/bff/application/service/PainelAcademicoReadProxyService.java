package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelAcademicoReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelAcademicoUseCase;
import reactor.core.publisher.Mono;

public class PainelAcademicoReadProxyService implements ConsultarPainelAcademicoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelAcademicoReadPort dashboardAcademicoReadPort;

    public PainelAcademicoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelAcademicoReadPort dashboardAcademicoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardAcademicoReadPort = dashboardAcademicoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardAcademicoReadPort.consultar(query, context));
    }
}
