package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.UUID;

public record PessoaResumo(
        UUID id,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        Boolean ativo) {
}
