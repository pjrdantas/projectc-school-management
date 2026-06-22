package br.com.escola.seguranca.adapter.in.web.dto;

import java.util.UUID;

public record AuthContextResponse(
        UUID usuarioId,
        UUID escolaId,
        String escolaNome,
        String username
) {}
