package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelDiretorReadPort;
import br.com.escola.bff.application.usecase.ConsultarPainelDiretorUseCase;
import reactor.core.publisher.Mono;

public class PainelDiretorReadProxyService implements ConsultarPainelDiretorUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelDiretorReadPort dashboardDiretorReadPort;

    public PainelDiretorReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelDiretorReadPort dashboardDiretorReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardDiretorReadPort = dashboardDiretorReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardDiretorReadPort.consultar(query, context));
    }
}
