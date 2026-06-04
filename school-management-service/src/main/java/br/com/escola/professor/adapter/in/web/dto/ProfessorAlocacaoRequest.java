package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ProfessorAlocacaoRequest(
        @NotNull UUID turmaDisciplinaId,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo) {
}
