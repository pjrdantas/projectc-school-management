package br.com.escola.academiccatalog.application.dto;

import java.time.LocalDate;

public record PeriodoLetivoInput(
        String nome,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim) {
}
