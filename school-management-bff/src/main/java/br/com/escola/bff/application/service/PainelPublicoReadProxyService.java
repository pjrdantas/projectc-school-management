package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelPublicoReadPort;
import br.com.escola.bff.application.usecase.ListarPainelPublicoUseCase;
import reactor.core.publisher.Mono;

public class PainelPublicoReadProxyService implements ListarPainelPublicoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelPublicoReadPort dashboardPublicoReadPort;

    public PainelPublicoReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelPublicoReadPort dashboardPublicoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardPublicoReadPort = dashboardPublicoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarPublicos(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardPublicoReadPort.listarPublicos(query, context));
    }
}
