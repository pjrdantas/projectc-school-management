package br.com.escola.enrollment.adapter.in.web;

import java.util.UUID;

import java.time.LocalDateTime;

public record MatriculaResponse(
        UUID id,
        UUID alunoId,
        UUID turmaId,
        UUID periodoLetivoId,
        String status,
        LocalDateTime createdAt
) {
}
