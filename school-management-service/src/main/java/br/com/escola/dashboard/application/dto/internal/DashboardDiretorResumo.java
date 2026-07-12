package br.com.escola.dashboard.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record DashboardDiretorResumo(
        UUID escolaId,
        String escolaNome,
        long totalMatriculas,
        long matriculasPendentes,
        long matriculasConcluidas,
        long matriculasEfetivadas,
        long matriculasAptasRematricula,
        long alunosAtivos,
        long alunosInativos,
        long turmasAtivas,
        long turmasLotadas,
        long professoresAlocados,
        long aulasRealizadas,
        long avaliacoesRegistradas,
        long avaliacoesComNotasPendentes,
        long boletinsFechados,
        long historicosInternosGerados,
        long transferencias,
        long solicitacoesExclusaoPendentes,
        long matriculasComDocumentosPendentes,
        List<DashboardMatriculaStatusResumo> matriculasPorStatus,
        List<DashboardTurmaVagaResumo> turmasComVagas) {
}
