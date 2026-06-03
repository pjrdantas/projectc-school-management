package br.com.escola.catalogo.application.dto;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoLetivoOutput(
        UUID id,
        String nome,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo,
        LocalDateTime createdAt) {
}
