package br.com.escola.academiccatalog.application.dto;

import java.time.LocalDate;

public record PeriodoLetivoInput(
        String nome,
        LocalDate dataInicio,
        LocalDate dataFim) {
}
