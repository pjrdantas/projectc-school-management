package br.com.escola.bff.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoLetivoCreateCommand(
        String nome,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        UUID escolaId
) {
}
