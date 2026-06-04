package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.List;

public record DashboardAcademicoResponse(
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
