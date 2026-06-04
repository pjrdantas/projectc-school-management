package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.List;

public record DashboardDiretorResponse(
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
