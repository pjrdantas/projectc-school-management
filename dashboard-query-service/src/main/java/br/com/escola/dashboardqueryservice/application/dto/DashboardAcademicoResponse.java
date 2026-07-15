package br.com.escola.dashboardqueryservice.application.dto;

import java.util.List;
import java.util.UUID;

public record DashboardAcademicoResponse(
        UUID escolaId,
        String escolaNome,
        long totalMatriculas,
        long matriculasAguardandoDocumentos,
        long matriculasConcluidas,
        long matriculasEfetivadas,
        long matriculasAptasRematricula,
        long boletinsFechados,
        long historicosInternosGerados,
        long alunosAprovados,
        long alunosReprovados,
        List<DashboardMatriculaStatusResponse> matriculasPorStatus,
        List<DashboardTurmaVagaResponse> turmasComVagas) {
}
