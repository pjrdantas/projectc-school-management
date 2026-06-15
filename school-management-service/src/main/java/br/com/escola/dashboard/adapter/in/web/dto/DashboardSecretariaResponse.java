package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record DashboardSecretariaResponse(
        UUID escolaId,
        String escolaNome,
        long totalMatriculas,
        long matriculasSolicitadas,
        long matriculasEmAndamento,
        long matriculasAguardandoDocumentos,
        long matriculasAguardandoHistoricoEscolar,
        long matriculasComDocumentosPendentes,
        long matriculasAptasRematricula,
        long boletinsFechados,
        long historicosInternosGerados,
        long transferencias,
        long solicitacoesExclusaoPendentes,
        List<DashboardMatriculaStatusResponse> matriculasPorStatus,
        List<DashboardTurmaVagaResponse> turmasComVagas) {
}
