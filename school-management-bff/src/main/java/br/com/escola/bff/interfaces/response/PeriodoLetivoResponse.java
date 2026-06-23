package br.com.escola.bff.interfaces.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PeriodoLetivoResponse(
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
