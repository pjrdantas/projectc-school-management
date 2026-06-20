package br.com.escola.catalog.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PeriodoLetivoResponse(
        UUID id,
        String nome,
        int ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativo,
        UUID escolaId,
        LocalDateTime createdAt) {
}
