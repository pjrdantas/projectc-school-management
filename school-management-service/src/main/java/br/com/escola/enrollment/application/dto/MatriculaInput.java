package br.com.escola.enrollment.application.dto;

public record MatriculaInput(
        Long alunoId,
        Long turmaId,
        Long periodoLetivoId) {
}
