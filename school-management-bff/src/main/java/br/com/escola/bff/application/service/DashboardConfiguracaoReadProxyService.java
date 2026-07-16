package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardConfiguracaoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardConfiguracaoReadPort;
import br.com.escola.bff.application.usecase.ListarDashboardConfiguracaoUseCase;
import reactor.core.publisher.Mono;

public class DashboardConfiguracaoReadProxyService implements ListarDashboardConfiguracaoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardConfiguracaoReadPort dashboardConfiguracaoReadPort;
    private final MonolithDashboardConfiguracaoReadPort monolithDashboardConfiguracaoReadPort;

    public DashboardConfiguracaoReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardConfiguracaoReadPort dashboardConfiguracaoReadPort,
            MonolithDashboardConfiguracaoReadPort monolithDashboardConfiguracaoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardConfiguracaoReadPort = dashboardConfiguracaoReadPort;
        this.monolithDashboardConfiguracaoReadPort = monolithDashboardConfiguracaoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarDashboards(
            String authorization,
            String correlationId,
            UUID publicoDashboardId,
            String publicoCodigo) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardConfiguracaoReadPort
                        .listarDashboards(publicoDashboardId, publicoCodigo, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardConfiguracaoReadPort
                                .listarDashboards(publicoDashboardId, publicoCodigo, query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardConfiguracaoReadPort
                                .listarDashboards(publicoDashboardId, publicoCodigo, query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
