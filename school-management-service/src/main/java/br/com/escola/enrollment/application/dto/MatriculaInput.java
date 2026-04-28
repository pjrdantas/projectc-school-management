package br.com.escola.enrollment.application.dto;

import java.util.UUID;

public record MatriculaInput(
        UUID alunoId,
        UUID turmaId,
        UUID periodoLetivoId) {
}
