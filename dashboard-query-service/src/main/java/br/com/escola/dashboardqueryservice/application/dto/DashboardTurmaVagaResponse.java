package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record DashboardTurmaVagaResponse(
        UUID turmaId,
        String turmaNome,
        int capacidade,
        long vagasOcupadas,
        long vagasDisponiveis) {
}
