package br.com.escola.bff.application.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.exception.DownstreamUnavailableException;
import br.com.escola.bff.application.port.out.DashboardIndicadorSnapshotReadPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MonolithDashboardIndicadorSnapshotReadPort;
import br.com.escola.bff.application.usecase.ListarDashboardIndicadorSnapshotUseCase;
import reactor.core.publisher.Mono;

public class DashboardIndicadorSnapshotReadProxyService implements ListarDashboardIndicadorSnapshotUseCase {

    private final InternalAuthContextPort authContextPort;
    private final DashboardIndicadorSnapshotReadPort dashboardIndicadorSnapshotReadPort;
    private final MonolithDashboardIndicadorSnapshotReadPort monolithDashboardIndicadorSnapshotReadPort;

    public DashboardIndicadorSnapshotReadProxyService(
            InternalAuthContextPort authContextPort,
            DashboardIndicadorSnapshotReadPort dashboardIndicadorSnapshotReadPort,
            MonolithDashboardIndicadorSnapshotReadPort monolithDashboardIndicadorSnapshotReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardIndicadorSnapshotReadPort = dashboardIndicadorSnapshotReadPort;
        this.monolithDashboardIndicadorSnapshotReadPort = monolithDashboardIndicadorSnapshotReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> listarPorPublicoCodigo(
            String authorization,
            String correlationId,
            String publicoCodigo,
            LocalDate referenciaData) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardIndicadorSnapshotReadPort
                        .listarPorPublicoCodigo(publicoCodigo, referenciaData, query, context)
                        .onErrorMap(
                                DownstreamUnavailableException.class,
                                DashboardQueryReadFailureException::new))
                .onErrorResume(DownstreamUnavailableException.class,
                        error -> monolithDashboardIndicadorSnapshotReadPort.listarPorPublicoCodigo(publicoCodigo, referenciaData, query))
                .onErrorResume(DashboardQueryReadFailureException.class,
                        error -> monolithDashboardIndicadorSnapshotReadPort.listarPorPublicoCodigo(publicoCodigo, referenciaData, query));
    }

    private static final class DashboardQueryReadFailureException extends RuntimeException {

        private DashboardQueryReadFailureException(DownstreamUnavailableException cause) {
            super(cause);
        }
    }
}
