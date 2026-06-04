package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

public record DashboardTurmaVagaResponse(
        UUID turmaId,
        String turmaNome,
        int capacidade,
        long vagasOcupadas,
        long vagasDisponiveis) {
}
