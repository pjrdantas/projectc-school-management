package br.com.escola.institutionaltenantservice.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record VinculoUsuarioEscola(
        UUID id,
        UUID usuarioId,
        UUID escolaId,
        LocalDateTime createdAt) {
}
