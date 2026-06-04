package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatriculaRematriculaRequest(
        @NotNull
        UUID turmaId,
        @NotNull
        UUID periodoLetivoId,
        @Size(max = 4000)
        String observacao) {
}
