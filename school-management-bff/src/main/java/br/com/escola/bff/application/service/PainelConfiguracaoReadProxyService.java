package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelConfiguracaoReadPort;
import br.com.escola.bff.application.usecase.ListarPainelConfiguracaoUseCase;
import reactor.core.publisher.Mono;

public class PainelConfiguracaoReadProxyService implements ListarPainelConfiguracaoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelConfiguracaoReadPort dashboardConfiguracaoReadPort;

    public PainelConfiguracaoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelConfiguracaoReadPort dashboardConfiguracaoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardConfiguracaoReadPort = dashboardConfiguracaoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarPainels(
            String authorization,
            String correlationId,
            UUID publicoPainelId,
            String publicoCodigo) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardConfiguracaoReadPort
                        .listarPainels(publicoPainelId, publicoCodigo, query, context));
    }
}
