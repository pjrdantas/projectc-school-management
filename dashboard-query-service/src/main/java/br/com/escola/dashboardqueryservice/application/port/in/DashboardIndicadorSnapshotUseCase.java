package br.com.escola.dashboardqueryservice.application.port.in;

import java.time.LocalDate;
import java.util.List;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.DashboardIndicadorSnapshotResponse;

public interface DashboardIndicadorSnapshotUseCase {

    List<DashboardIndicadorSnapshotResponse> listarPorPublicoCodigo(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            LocalDate referenciaData);
}
