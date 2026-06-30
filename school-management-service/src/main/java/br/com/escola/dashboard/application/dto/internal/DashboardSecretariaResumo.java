package br.com.escola.dashboard.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record DashboardSecretariaResumo(
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
        List<DashboardMatriculaStatusResumo> matriculasPorStatus,
        List<DashboardTurmaVagaResumo> turmasComVagas) {
}
