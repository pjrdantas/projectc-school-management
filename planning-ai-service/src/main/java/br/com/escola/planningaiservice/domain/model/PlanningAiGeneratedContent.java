package br.com.escola.planningaiservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlanningAiGeneratedContent(
        UUID id,
        UUID escolaId,
        UUID planejamentoBimestralId,
        UUID interacaoId,
        String titulo,
        String conteudo,
        Integer versao,
        String hashConteudo,
        boolean aprovadoPeloProfessor,
        boolean reutilizavel,
        boolean ativo,
        String status,
        String tipoConteudo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
