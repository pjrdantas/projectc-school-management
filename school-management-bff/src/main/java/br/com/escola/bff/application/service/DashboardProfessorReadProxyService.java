package br.com.escola.bff.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardProfessorReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardProfessorReadPort;
import br.com.escola.bff.application.usecase.ConsultarDashboardProfessorUseCase;
import reactor.core.publisher.Mono;

public class DashboardProfessorReadProxyService implements ConsultarDashboardProfessorUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardProfessorReadPort dashboardProfessorReadPort;
    private final MonolithDashboardProfessorReadPort monolithDashboardProfessorReadPort;

    public DashboardProfessorReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardProfessorReadPort dashboardProfessorReadPort,
            MonolithDashboardProfessorReadPort monolithDashboardProfessorReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardProfessorReadPort = dashboardProfessorReadPort;
        this.monolithDashboardProfessorReadPort = monolithDashboardProfessorReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(String authorization, String correlationId, UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardProfessorReadPort.consultar(professorId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardProfessorReadPort.consultar(professorId, query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardProfessorReadPort.consultar(professorId, query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
