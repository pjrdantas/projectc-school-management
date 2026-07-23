package br.com.escola.dashboardqueryservice.application.port.out;

import java.time.LocalDate;
import java.util.List;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;

public interface PainelIndicadorSnapshotPort {

    List<PainelIndicadorSnapshotResponse> listarPorPublicoCodigo(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            LocalDate referenciaData);
}

