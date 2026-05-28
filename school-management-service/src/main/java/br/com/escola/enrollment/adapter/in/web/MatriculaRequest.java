package br.com.escola.enrollment.adapter.in.web;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MatriculaRequest(
        @NotNull(message = "alunoId é obrigatório")
        UUID alunoId,

        @NotNull(message = "turmaId é obrigatório")
        UUID turmaId,

        @NotNull(message = "periodoLetivoId é obrigatório")
        UUID periodoLetivoId,

        String tipoMatricula,
        String observacao
) {
}
