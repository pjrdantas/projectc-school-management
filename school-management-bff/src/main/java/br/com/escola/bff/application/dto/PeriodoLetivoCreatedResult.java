package br.com.escola.bff.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PeriodoLetivoCreatedResult(
        UUID id,
        String nome,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo,
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt
) {
}
