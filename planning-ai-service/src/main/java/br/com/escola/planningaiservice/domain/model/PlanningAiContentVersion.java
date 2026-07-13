package br.com.escola.planningaiservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlanningAiContentVersion(
        UUID id,
        UUID escolaId,
        UUID conteudoGeradoId,
        UUID alteradoPor,
        Integer numeroVersao,
        String conteudo,
        String motivoAlteracao,
        LocalDateTime createdAt) {
}
