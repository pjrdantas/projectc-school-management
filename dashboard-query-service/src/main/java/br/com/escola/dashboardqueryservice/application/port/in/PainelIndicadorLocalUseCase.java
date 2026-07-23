package br.com.escola.dashboardqueryservice.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;

public interface PainelIndicadorLocalUseCase {

    List<PainelIndicadorSnapshotResponse> listar(InternalRequestContext context, UUID publicoId, LocalDate referenciaData);

    PainelIndicadorSnapshotResponse salvar(InternalRequestContext context, PainelIndicadorSnapshotCommand command);

    void excluir(InternalRequestContext context, UUID snapshotId);

    List<PainelIndicadorHistoricoResponse> historico(
            InternalRequestContext context,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim);
}
