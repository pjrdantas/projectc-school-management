package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelSecretariaReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelSecretariaUseCase;
import reactor.core.publisher.Mono;

public class PainelSecretariaReadProxyService implements ConsultarPainelSecretariaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelSecretariaReadPort dashboardSecretariaReadPort;

    public PainelSecretariaReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelSecretariaReadPort dashboardSecretariaReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardSecretariaReadPort = dashboardSecretariaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardSecretariaReadPort.consultar(query, context));
    }
}
