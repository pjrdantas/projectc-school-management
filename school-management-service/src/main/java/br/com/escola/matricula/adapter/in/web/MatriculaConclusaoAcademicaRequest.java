package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatriculaConclusaoAcademicaRequest(
        @NotNull
        UUID boletimId,
        @Size(max = 4000)
        String observacao) {
}
