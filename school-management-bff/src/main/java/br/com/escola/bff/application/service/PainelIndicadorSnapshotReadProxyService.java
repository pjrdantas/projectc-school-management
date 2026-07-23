package br.com.escola.bff.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.PainelIndicadorSnapshotReadPort;
import br.com.escola.bff.application.usecase.ListarPainelIndicadorSnapshotUseCase;
import reactor.core.publisher.Mono;

public class PainelIndicadorSnapshotReadProxyService implements ListarPainelIndicadorSnapshotUseCase {

    private final InternalAuthContextPort authContextPort;
    private final PainelIndicadorSnapshotReadPort dashboardIndicadorSnapshotReadPort;

    public PainelIndicadorSnapshotReadProxyService(
            InternalAuthContextPort authContextPort,
            PainelIndicadorSnapshotReadPort dashboardIndicadorSnapshotReadPort) {
        this.authContextPort = authContextPort;
        this.dashboardIndicadorSnapshotReadPort = dashboardIndicadorSnapshotReadPort;
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
                        .listarPorPublicoCodigo(publicoCodigo, referenciaData, query, context));
    }

    @Override
    public Mono<ResponseEntity<String>> listarPorPublicoId(
            String authorization, String correlationId, UUID publicoId, LocalDate referenciaData) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> dashboardIndicadorSnapshotReadPort
                        .listarPorPublicoId(publicoId, referenciaData, query, context));
    }
}
