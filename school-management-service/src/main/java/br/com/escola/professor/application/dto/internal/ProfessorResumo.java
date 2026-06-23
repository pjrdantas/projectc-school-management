package br.com.escola.professor.application.dto.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfessorResumo(
        UUID id,
        UUID pessoaId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String registroProfissional,
        String formacao,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
