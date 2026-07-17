package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelFrontendReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelFrontendUseCase;
import reactor.core.publisher.Mono;

public class PainelFrontendReadProxyService implements ConsultarPainelFrontendUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelFrontendReadPort dashboardFrontendReadPort;

    public PainelFrontendReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelFrontendReadPort dashboardFrontendReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardFrontendReadPort = dashboardFrontendReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(
            String authorization,
            String correlationId,
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardFrontendReadPort.consultar(publicoCodigo, usuarioId, professorId, query, context));
    }
}
