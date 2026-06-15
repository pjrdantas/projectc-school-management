package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record DashboardProfessorResponse(
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
        List<DashboardProfessorTurmaResponse> turmas) {
}
