package br.com.escola.bff.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SerieCreatedResult(
        UUID id,
        String nome,
        Integer ordem,
        String nivelEnsino,
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt
) {
}
