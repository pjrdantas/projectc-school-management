package br.com.escola.institutionaltenantservice.application.dto;

import java.util.UUID;

public record TenantAtivoResponse(
        UUID escolaId,
        String escolaNome) {
}
