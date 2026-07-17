package br.com.escola.dashboardqueryservice.application.dto;

import java.util.UUID;

public record PainelTurmaVagaResponse(
        UUID turmaId,
        String turmaNome,
        int capacidade,
        long vagasOcupadas,
        long vagasDisponiveis) {
}

