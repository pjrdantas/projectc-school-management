package br.com.escola.catalog.application.command;

import java.util.UUID;

public record CreateTurmaCommand(
        String codigo,
        String nome,
        int capacidade,
        UUID periodoLetivoId,
        UUID serieId,
        UUID turnoId) {
}
