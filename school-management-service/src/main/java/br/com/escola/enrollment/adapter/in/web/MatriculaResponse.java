package br.com.escola.enrollment.adapter.in.web;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.escola.enrollment.application.dto.MatriculaEtapaOutput;

public record MatriculaResponse(
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
        List<MatriculaEtapaOutput> etapas
) {
}
