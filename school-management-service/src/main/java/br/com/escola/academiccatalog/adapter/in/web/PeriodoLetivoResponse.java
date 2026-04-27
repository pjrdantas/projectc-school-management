package br.com.escola.academiccatalog.adapter.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoLetivoResponse(
        Long id,
        String nome,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDateTime createdAt
) {
}
