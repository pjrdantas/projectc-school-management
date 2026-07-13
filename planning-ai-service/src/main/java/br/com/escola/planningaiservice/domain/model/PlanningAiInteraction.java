package br.com.escola.planningaiservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlanningAiInteraction(
        UUID id,
        UUID escolaId,
        UUID planejamentoBimestralId,
        UUID usuarioId,
        String promptProfessor,
        String respostaIa,
        String modeloIa,
        Integer tokensEntrada,
        Integer tokensSaida,
        BigDecimal custoEstimado,
        LocalDateTime createdAt) {
}
