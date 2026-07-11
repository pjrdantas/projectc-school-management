package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaResumoResponse(
        UUID id,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        Boolean ativo) {
}


