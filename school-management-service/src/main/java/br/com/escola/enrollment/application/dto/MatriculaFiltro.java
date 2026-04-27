package br.com.escola.enrollment.application.dto;

public record MatriculaFiltro(
        Long alunoId,
        Long turmaId,
        Long periodoLetivoId,
        String status) {
}
