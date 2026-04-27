package br.com.escola.academiccatalog.application.dto;

import java.time.LocalDateTime;

public record TurmaOutput(
        Long id,
        String codigo,
        String nome,
        Integer capacidade,
        Long periodoLetivoId,
        LocalDateTime createdAt) {
}
