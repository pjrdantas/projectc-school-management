package br.com.escola.dashboardqueryservice.application.dto;

import java.util.List;
import java.util.UUID;

public record PainelSecretariaResponse(
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
        List<PainelMatriculaStatusResponse> matriculasPorStatus,
        List<PainelTurmaVagaResponse> turmasComVagas) {
}

