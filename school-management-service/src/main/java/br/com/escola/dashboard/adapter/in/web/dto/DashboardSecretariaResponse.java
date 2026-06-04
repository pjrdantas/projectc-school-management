package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.List;

public record DashboardSecretariaResponse(
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
