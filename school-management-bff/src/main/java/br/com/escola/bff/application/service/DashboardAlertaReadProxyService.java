package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardAlertaReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardAlertaReadPort;
import br.com.escola.bff.application.usecase.ConsultarDashboardAlertaUseCase;
import reactor.core.publisher.Mono;

public class DashboardAlertaReadProxyService implements ConsultarDashboardAlertaUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardAlertaReadPort dashboardAlertaReadPort;
    private final MonolithDashboardAlertaReadPort monolithDashboardAlertaReadPort;

    public DashboardAlertaReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardAlertaReadPort dashboardAlertaReadPort,
            MonolithDashboardAlertaReadPort monolithDashboardAlertaReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardAlertaReadPort = dashboardAlertaReadPort;
        this.monolithDashboardAlertaReadPort = monolithDashboardAlertaReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(
            String authorization,
            String correlationId,
            String publicoCodigo,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardAlertaReadPort.consultar(publicoCodigo, professorId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardAlertaReadPort.consultar(publicoCodigo, professorId, query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardAlertaReadPort.consultar(publicoCodigo, professorId, query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
