package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.UUID;

public record PessoaResumoInternalResponse(
        UUID id,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        Boolean ativo) {
}
