package br.com.escola.bff.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisciplinaCreatedResult(
        UUID id,
        String nome,
        Integer cargaHoraria,
        String status,
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt
) {
}
