package br.com.escola.institutionaltenantservice.application.dto;

import java.util.UUID;

public record TenantEscolaResponse(
        UUID escolaId,
        String escolaNome,
        boolean ativa) {
}
