package br.com.escola.dashboardqueryservice.application.dto;

import java.util.List;
import java.util.UUID;

public record PainelProfessorResponse(
        UUID escolaId,
        String escolaNome,
        UUID professorId,
        long turmasVinculadas,
        long alocacoesAtivas,
        long aulasPlanejadas,
        long aulasRealizadas,
        long frequenciasPendentes,
        long avaliacoesRegistradas,
        long avaliacoesComNotasPendentes,
        long planejamentosBimestrais,
        long planejamentosBimestraisPendentes,
        List<PainelProfessorTurmaResponse> turmas) {
}

