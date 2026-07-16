package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardFrontendReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardFrontendReadPort;
import br.com.escola.bff.application.usecase.ConsultarDashboardFrontendUseCase;
import reactor.core.publisher.Mono;

public class DashboardFrontendReadProxyService implements ConsultarDashboardFrontendUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardFrontendReadPort dashboardFrontendReadPort;
    private final MonolithDashboardFrontendReadPort monolithDashboardFrontendReadPort;

    public DashboardFrontendReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardFrontendReadPort dashboardFrontendReadPort,
            MonolithDashboardFrontendReadPort monolithDashboardFrontendReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardFrontendReadPort = dashboardFrontendReadPort;
        this.monolithDashboardFrontendReadPort = monolithDashboardFrontendReadPort;
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
                .flatMap(context -> dashboardFrontendReadPort.consultar(publicoCodigo, usuarioId, professorId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardFrontendReadPort.consultar(publicoCodigo, usuarioId, professorId, query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardFrontendReadPort.consultar(publicoCodigo, usuarioId, professorId, query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
