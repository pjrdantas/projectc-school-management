package br.com.escola.bff.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TurmaCreatedResult(
        UUID id,
        String codigo,
        String nome,
        Integer capacidade,
        UUID periodoLetivoId,
        UUID serieId,
        String serieNome,
        String turno,
        String status,
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt
) {
}
