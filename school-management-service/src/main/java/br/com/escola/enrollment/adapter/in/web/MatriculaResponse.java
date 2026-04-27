package br.com.escola.enrollment.adapter.in.web;

import java.time.LocalDateTime;

public record MatriculaResponse(
        Long id,
        Long alunoId,
        Long turmaId,
        Long periodoLetivoId,
        String status,
        LocalDateTime createdAt
) {
}
