package br.com.escola.enrollment.application.dto;

import java.time.LocalDateTime;

public record MatriculaOutput(
        Long id,
        Long alunoId,
        Long turmaId,
        Long periodoLetivoId,
        String status,
        LocalDateTime createdAt) {
}
