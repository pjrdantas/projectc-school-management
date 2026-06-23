package br.com.escola.professor.adapter.in.web.dto.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfessorInternalResponse(
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
