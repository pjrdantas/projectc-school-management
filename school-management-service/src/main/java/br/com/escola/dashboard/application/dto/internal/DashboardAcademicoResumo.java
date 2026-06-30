package br.com.escola.dashboard.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record DashboardAcademicoResumo(
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
        List<DashboardMatriculaStatusResumo> matriculasPorStatus,
        List<DashboardTurmaVagaResumo> turmasComVagas) {
}
