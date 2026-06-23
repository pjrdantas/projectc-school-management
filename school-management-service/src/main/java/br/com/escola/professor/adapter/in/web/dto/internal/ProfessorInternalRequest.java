package br.com.escola.professor.adapter.in.web.dto.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfessorInternalRequest(
        @NotNull UUID funcionarioId,
        @Size(max = 80) String registroProfissional,
        @Size(max = 150) String formacao,
        Boolean ativo
) {
}
