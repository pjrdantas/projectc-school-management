package br.com.escola.catalogo.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoLetivoInput(
        String nome,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        UUID escolaId) {
}
