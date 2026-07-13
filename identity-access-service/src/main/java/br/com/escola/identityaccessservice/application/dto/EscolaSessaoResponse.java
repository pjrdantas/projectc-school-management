package br.com.escola.identityaccessservice.application.dto;

import java.util.UUID;

public record EscolaSessaoResponse(
        UUID escolaId,
        String escolaNome,
        boolean ativa) {
}
