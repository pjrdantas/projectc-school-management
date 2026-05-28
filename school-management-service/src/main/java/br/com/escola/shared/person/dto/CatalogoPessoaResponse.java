package br.com.escola.shared.person.dto;

import java.util.UUID;

public record CatalogoPessoaResponse(
        UUID id,
        String codigo,
        String descricao
) {
}
