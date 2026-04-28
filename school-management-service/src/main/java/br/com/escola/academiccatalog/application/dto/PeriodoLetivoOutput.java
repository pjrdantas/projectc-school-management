package br.com.escola.academiccatalog.application.dto;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoLetivoOutput(
        UUID id,
        String nome,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDateTime createdAt) {
}
