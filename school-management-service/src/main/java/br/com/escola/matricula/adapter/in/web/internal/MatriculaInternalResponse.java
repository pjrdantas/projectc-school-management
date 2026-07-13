package br.com.escola.matricula.adapter.in.web.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MatriculaInternalResponse(
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
        List<MatriculaEtapaInternalResponse> etapas) {
}
