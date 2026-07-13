package br.com.escola.enrollmentdocumentservice.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MatriculaResponse(
        UUID id,
        UUID alunoId,
        UUID turmaId,
        UUID escolaId,
        String escolaNome,
        UUID serieId,
        String serieNome,
        UUID periodoLetivoId,
        String status,
        String tipoMatricula,
        LocalDate dataMatricula,
        String observacao,
        LocalDateTime createdAt,
        List<MatriculaEtapaResponse> etapas) {
}
