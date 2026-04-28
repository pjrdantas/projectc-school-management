package br.com.escola.academiccatalog.application.dto;

import java.util.UUID;

import java.time.LocalDateTime;

public record TurmaOutput(
        UUID id,
        String codigo,
        String nome,
        Integer capacidade,
        UUID periodoLetivoId,
        LocalDateTime createdAt) {
}
