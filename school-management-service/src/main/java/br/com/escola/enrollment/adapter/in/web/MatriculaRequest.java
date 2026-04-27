package br.com.escola.enrollment.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record MatriculaRequest(
        @NotNull(message = "alunoId é obrigatório")
        Long alunoId,

        @NotNull(message = "turmaId é obrigatório")
        Long turmaId,

        @NotNull(message = "periodoLetivoId é obrigatório")
        Long periodoLetivoId
) {
}
