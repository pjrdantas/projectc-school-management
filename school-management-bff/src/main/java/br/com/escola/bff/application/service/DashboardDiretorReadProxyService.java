package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardDiretorReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardDiretorReadPort;
import br.com.escola.bff.application.usecase.ConsultarDashboardDiretorUseCase;
import reactor.core.publisher.Mono;

public class DashboardDiretorReadProxyService implements ConsultarDashboardDiretorUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardDiretorReadPort dashboardDiretorReadPort;
    private final MonolithDashboardDiretorReadPort monolithDashboardDiretorReadPort;

    public DashboardDiretorReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardDiretorReadPort dashboardDiretorReadPort,
            MonolithDashboardDiretorReadPort monolithDashboardDiretorReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardDiretorReadPort = dashboardDiretorReadPort;
        this.monolithDashboardDiretorReadPort = monolithDashboardDiretorReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardDiretorReadPort.consultar(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardDiretorReadPort.consultar(query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardDiretorReadPort.consultar(query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
