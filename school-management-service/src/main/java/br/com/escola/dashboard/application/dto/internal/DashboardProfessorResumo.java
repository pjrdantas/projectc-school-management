package br.com.escola.dashboard.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record DashboardProfessorResumo(
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
        List<DashboardProfessorTurmaResumo> turmas) {
}
