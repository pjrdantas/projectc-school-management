package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

public record MatriculaConclusaoAcademicaResponse(
        UUID matriculaId,
        UUID boletimId,
        String resultadoFinal,
        String status,
        String observacao) {
}
