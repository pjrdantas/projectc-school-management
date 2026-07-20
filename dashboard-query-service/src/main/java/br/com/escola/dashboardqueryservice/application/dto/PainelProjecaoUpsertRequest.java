package br.com.escola.dashboardqueryservice.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public record PainelProjecaoUpsertRequest(
        @NotNull TipoPainelProjecao tipo,
        String publicoCodigo,
        UUID professorId,
        UUID usuarioId,
        LocalDate referenciaData,
        @NotNull JsonNode payload) {
}
