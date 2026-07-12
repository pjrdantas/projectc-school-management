package br.com.escola.dashboard.application.dto.internal;

import java.util.UUID;

public record DashboardTurmaVagaResumo(
        UUID turmaId,
        String turmaNome,
        int capacidade,
        long vagasOcupadas,
        long vagasDisponiveis) {
}
