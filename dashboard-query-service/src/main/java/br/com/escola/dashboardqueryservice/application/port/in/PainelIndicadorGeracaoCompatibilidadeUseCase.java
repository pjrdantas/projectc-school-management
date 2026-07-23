package br.com.escola.dashboardqueryservice.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;

public interface PainelIndicadorGeracaoCompatibilidadeUseCase {

    List<PainelIndicadorSnapshotResponse> consultarPorPublico(
            InternalRequestContext context, String publicoCodigo, LocalDate referenciaData);

    List<PainelIndicadorSnapshotResponse> consultarPorProfessor(
            InternalRequestContext context, UUID professorId, LocalDate referenciaData);
}
