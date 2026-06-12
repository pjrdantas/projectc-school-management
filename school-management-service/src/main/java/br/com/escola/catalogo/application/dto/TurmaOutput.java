package br.com.escola.catalogo.application.dto;

import java.util.UUID;

import java.time.LocalDateTime;

public record TurmaOutput(
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
        LocalDateTime createdAt) {
}
