package br.com.escola.dashboardqueryservice.application.dto;

import java.util.List;
import java.util.UUID;

public record DashboardDiretorResponse(
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
        List<DashboardMatriculaStatusResponse> matriculasPorStatus,
        List<DashboardTurmaVagaResponse> turmasComVagas) {
}
