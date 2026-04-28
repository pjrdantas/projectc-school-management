package br.com.escola.enrollment.application.dto;

import java.util.UUID;

public record MatriculaFiltro(
        UUID alunoId,
        UUID turmaId,
        UUID periodoLetivoId,
        String status) {
}
