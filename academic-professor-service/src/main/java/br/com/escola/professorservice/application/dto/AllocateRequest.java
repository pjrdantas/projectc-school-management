package br.com.escola.professorservice.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AllocateRequest(
        @NotNull UUID turmaDisciplinaId,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo
) {
}

