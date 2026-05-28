package br.com.escola.enrollment.application.dto;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MatriculaOutput(
        UUID id,
        UUID alunoId,
        UUID turmaId,
        UUID serieId,
        String serieNome,
        UUID periodoLetivoId,
        String status,
        String tipoMatricula,
        LocalDate dataMatricula,
        String observacao,
        LocalDateTime createdAt,
        List<MatriculaEtapaOutput> etapas) {
}
