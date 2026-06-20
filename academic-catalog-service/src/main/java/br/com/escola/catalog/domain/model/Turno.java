package br.com.escola.catalog.domain.model;

import java.util.UUID;

public record Turno(
        UUID id,
        String codigo,
        String descricao) {

    public Turno {
        id = CatalogAssertions.notNull(id, "id");
        codigo = CatalogAssertions.notBlank(codigo, "codigo").toUpperCase(java.util.Locale.ROOT);
        descricao = CatalogAssertions.notBlank(descricao, "descricao");
    }
}

