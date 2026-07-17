package br.com.escola.dashboardqueryservice.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorSnapshotUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelIndicadorSnapshotPort;

@Service
public class PainelIndicadorSnapshotService implements PainelIndicadorSnapshotUseCase {

    private final PainelIndicadorSnapshotPort dashboardIndicadorSnapshotPort;

    public PainelIndicadorSnapshotService(PainelIndicadorSnapshotPort dashboardIndicadorSnapshotPort) {
        this.dashboardIndicadorSnapshotPort = dashboardIndicadorSnapshotPort;
    }

    @Override
    public List<PainelIndicadorSnapshotResponse> listarPorPublicoCodigo(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            LocalDate referenciaData) {
        return dashboardIndicadorSnapshotPort.listarPorPublicoCodigo(authorization, context, publicoCodigo, referenciaData);
    }
}

