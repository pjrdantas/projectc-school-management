package br.com.escola.dashboardqueryservice.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.port.in.DashboardIndicadorSnapshotUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.DashboardIndicadorSnapshotPort;

@Service
public class DashboardIndicadorSnapshotService implements DashboardIndicadorSnapshotUseCase {

    private final DashboardIndicadorSnapshotPort dashboardIndicadorSnapshotPort;

    public DashboardIndicadorSnapshotService(DashboardIndicadorSnapshotPort dashboardIndicadorSnapshotPort) {
        this.dashboardIndicadorSnapshotPort = dashboardIndicadorSnapshotPort;
    }

    @Override
    public List<DashboardIndicadorSnapshotResponse> listarPorPublicoCodigo(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            LocalDate referenciaData) {
        return dashboardIndicadorSnapshotPort.listarPorPublicoCodigo(authorization, context, publicoCodigo, referenciaData);
    }
}
