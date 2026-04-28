package br.com.escola.enrollment.application.dto;

import java.util.UUID;

import java.time.LocalDateTime;

public record MatriculaOutput(
        UUID id,
        UUID alunoId,
        UUID turmaId,
        UUID periodoLetivoId,
        String status,
        LocalDateTime createdAt) {
}
