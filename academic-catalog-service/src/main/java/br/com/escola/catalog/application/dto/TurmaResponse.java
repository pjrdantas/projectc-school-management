package br.com.escola.catalog.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TurmaResponse(
        UUID id,
        String codigo,
        String nome,
        int capacidade,
        UUID periodoLetivoId,
        UUID serieId,
        String serieNome,
        UUID turnoId,
        String turnoCodigo,
        boolean ativo,
        UUID escolaId,
        LocalDateTime createdAt) {
}
