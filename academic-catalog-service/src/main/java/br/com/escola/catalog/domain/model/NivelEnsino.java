package br.com.escola.catalog.domain.model;

import java.util.Locale;
import java.util.UUID;

public record NivelEnsino(
        UUID id,
        String codigo,
        String descricao) {

    public NivelEnsino {
        id = CatalogAssertions.notNull(id, "id");
        codigo = CatalogAssertions.notBlank(codigo, "codigo").toUpperCase(Locale.ROOT);
        descricao = CatalogAssertions.notBlank(descricao, "descricao");
    }
}
