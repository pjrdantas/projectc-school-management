package br.com.escola.catalog.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SerieResponse(
        UUID id,
        String nome,
        int ordem,
        UUID nivelEnsinoId,
        String nivelEnsinoCodigo,
        UUID escolaId,
        LocalDateTime createdAt) {
}
