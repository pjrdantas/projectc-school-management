package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardPublicoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardPublicoReadPort;
import br.com.escola.bff.application.usecase.ListarDashboardPublicoUseCase;
import reactor.core.publisher.Mono;

public class DashboardPublicoReadProxyService implements ListarDashboardPublicoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardPublicoReadPort dashboardPublicoReadPort;
    private final MonolithDashboardPublicoReadPort monolithDashboardPublicoReadPort;

    public DashboardPublicoReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardPublicoReadPort dashboardPublicoReadPort,
            MonolithDashboardPublicoReadPort monolithDashboardPublicoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardPublicoReadPort = dashboardPublicoReadPort;
        this.monolithDashboardPublicoReadPort = monolithDashboardPublicoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarPublicos(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardPublicoReadPort.listarPublicos(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardPublicoReadPort.listarPublicos(query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardPublicoReadPort.listarPublicos(query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
