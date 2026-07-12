package br.com.escola.seguranca.adapter.in.web.dto.internal;

import java.util.UUID;

public record EscolaSessaoInternalResponse(
        UUID escolaId,
        String escolaNome,
        boolean ativa
) {
}
