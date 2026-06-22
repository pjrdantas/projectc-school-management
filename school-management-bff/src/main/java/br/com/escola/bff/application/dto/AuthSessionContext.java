package br.com.escola.bff.application.dto;

import java.util.UUID;

public record AuthSessionContext(
        UUID usuarioId,
        UUID escolaId
) {}
