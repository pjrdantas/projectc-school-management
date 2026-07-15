package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardAcademicoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardAcademicoReadPort;
import br.com.escola.bff.application.usecase.ConsultarDashboardAcademicoUseCase;
import reactor.core.publisher.Mono;

public class DashboardAcademicoReadProxyService implements ConsultarDashboardAcademicoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardAcademicoReadPort dashboardAcademicoReadPort;
    private final MonolithDashboardAcademicoReadPort monolithDashboardAcademicoReadPort;

    public DashboardAcademicoReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardAcademicoReadPort dashboardAcademicoReadPort,
            MonolithDashboardAcademicoReadPort monolithDashboardAcademicoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardAcademicoReadPort = dashboardAcademicoReadPort;
        this.monolithDashboardAcademicoReadPort = monolithDashboardAcademicoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardAcademicoReadPort.consultar(query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardAcademicoReadPort.consultar(query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardAcademicoReadPort.consultar(query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
