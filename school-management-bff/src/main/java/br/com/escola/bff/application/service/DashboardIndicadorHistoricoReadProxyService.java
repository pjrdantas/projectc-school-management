package br.com.escola.bff.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardIndicadorHistoricoReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardIndicadorHistoricoReadPort;
import br.com.escola.bff.application.usecase.ConsultarDashboardIndicadorHistoricoUseCase;
import reactor.core.publisher.Mono;

public class DashboardIndicadorHistoricoReadProxyService implements ConsultarDashboardIndicadorHistoricoUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardIndicadorHistoricoReadPort dashboardIndicadorHistoricoReadPort;
    private final MonolithDashboardIndicadorHistoricoReadPort monolithDashboardIndicadorHistoricoReadPort;

    public DashboardIndicadorHistoricoReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardIndicadorHistoricoReadPort dashboardIndicadorHistoricoReadPort,
            MonolithDashboardIndicadorHistoricoReadPort monolithDashboardIndicadorHistoricoReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardIndicadorHistoricoReadPort = dashboardIndicadorHistoricoReadPort;
        this.monolithDashboardIndicadorHistoricoReadPort = monolithDashboardIndicadorHistoricoReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultarHistorico(
            String authorization,
            String correlationId,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardIndicadorHistoricoReadPort
                        .consultarHistorico(publicoCodigo, codigoIndicador, dataInicio, dataFim, professorId, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardIndicadorHistoricoReadPort
                                .consultarHistorico(publicoCodigo, codigoIndicador, dataInicio, dataFim, professorId, query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardIndicadorHistoricoReadPort
                                .consultarHistorico(publicoCodigo, codigoIndicador, dataInicio, dataFim, professorId, query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
