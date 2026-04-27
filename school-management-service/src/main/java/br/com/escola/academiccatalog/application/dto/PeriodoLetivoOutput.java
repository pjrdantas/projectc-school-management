package br.com.escola.academiccatalog.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoLetivoOutput(
        Long id,
        String nome,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDateTime createdAt) {
}
