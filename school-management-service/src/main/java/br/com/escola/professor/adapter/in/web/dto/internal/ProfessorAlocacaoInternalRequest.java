package br.com.escola.professor.adapter.in.web.dto.internal;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ProfessorAlocacaoInternalRequest(
        @NotNull UUID turmaDisciplinaId,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativo
) {
}
