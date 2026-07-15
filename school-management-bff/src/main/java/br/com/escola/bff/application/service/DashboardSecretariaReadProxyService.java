package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardSecretariaReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardSecretariaReadPort;
import br.com.escola.bff.application.usecase.ConsultarDashboardSecretariaUseCase;
import reactor.core.publisher.Mono;

public class DashboardSecretariaReadProxyService implements ConsultarDashboardSecretariaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardSecretariaReadPort dashboardSecretariaReadPort;
    private final MonolithDashboardSecretariaReadPort monolithDashboardSecretariaReadPort;

    public DashboardSecretariaReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardSecretariaReadPort dashboardSecretariaReadPort,
            MonolithDashboardSecretariaReadPort monolithDashboardSecretariaReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardSecretariaReadPort = dashboardSecretariaReadPort;
        this.monolithDashboardSecretariaReadPort = monolithDashboardSecretariaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardSecretariaReadPort.consultar(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardSecretariaReadPort.consultar(query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardSecretariaReadPort.consultar(query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
