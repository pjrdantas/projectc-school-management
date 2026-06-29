package br.com.escola.seguranca.application.dto.internal;

import java.util.UUID;

public record EscolaSessaoResumo(
        UUID escolaId,
        String escolaNome,
        boolean ativa
) {
}
