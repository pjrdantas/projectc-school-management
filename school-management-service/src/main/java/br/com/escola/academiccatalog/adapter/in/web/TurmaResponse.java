package br.com.escola.academiccatalog.adapter.in.web;

import java.time.LocalDateTime;

public record TurmaResponse(
        Long id,
        String codigo,
        String nome,
        Integer capacidade,
        Long periodoLetivoId,
        LocalDateTime createdAt
) {
}
