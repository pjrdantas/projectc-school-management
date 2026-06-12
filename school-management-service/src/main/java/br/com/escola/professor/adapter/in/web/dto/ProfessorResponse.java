package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfessorResponse(
        UUID id,
        UUID pessoaId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String registroProfissional,
        String formacao,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
