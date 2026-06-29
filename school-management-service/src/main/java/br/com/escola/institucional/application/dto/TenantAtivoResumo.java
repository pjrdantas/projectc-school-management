package br.com.escola.institucional.application.dto;

import java.util.UUID;

public record TenantAtivoResumo(
        UUID escolaId,
        String escolaNome,
        OrigemTenantAtivo origem
) {
}
