package br.com.escola.academiccatalog.adapter.in.web;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoLetivoResponse(
        UUID id,
        String nome,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDateTime createdAt
) {
}
