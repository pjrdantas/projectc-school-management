package br.com.escola.professorservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResumoResponse(
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

