package br.com.escola.compartilhado.pessoa.dto;

import java.util.UUID;

public record CatalogoPessoaResponse(
        UUID id,
        String codigo,
        String descricao
) {
}
