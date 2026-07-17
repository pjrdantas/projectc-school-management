package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelAlertaReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelAlertaUseCase;
import reactor.core.publisher.Mono;

public class PainelAlertaReadProxyService implements ConsultarPainelAlertaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelAlertaReadPort dashboardAlertaReadPort;

    public PainelAlertaReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelAlertaReadPort dashboardAlertaReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardAlertaReadPort = dashboardAlertaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(
            String authorization,
            String correlationId,
            String publicoCodigo,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardAlertaReadPort.consultar(publicoCodigo, professorId, query, context));
    }
}
