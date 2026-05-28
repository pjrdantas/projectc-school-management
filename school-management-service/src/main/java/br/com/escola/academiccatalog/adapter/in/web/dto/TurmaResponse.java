package br.com.escola.academiccatalog.adapter.in.web.dto;

import java.util.UUID;

import java.time.LocalDateTime;

public record TurmaResponse(
        UUID id,
        String codigo,
        String nome,
        Integer capacidade,
        UUID periodoLetivoId,
        UUID serieId,
        String serieNome,
        String turno,
        String status,
        LocalDateTime createdAt
) {
}
