package br.com.escola.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.valueobject.EscolaId;

public record Serie(
        UUID id,
        EscolaId escolaId,
        String nome,
        int ordem,
        UUID nivelEnsinoId,
        LocalDateTime createdAt) {

    public Serie {
        id = CatalogAssertions.notNull(id, "id");
        escolaId = CatalogAssertions.notNull(escolaId, "escolaId");
        nome = CatalogAssertions.notBlank(nome, "nome");
        ordem = CatalogAssertions.positive(ordem, "ordem");
        nivelEnsinoId = CatalogAssertions.notNull(nivelEnsinoId, "nivelEnsinoId");
        createdAt = CatalogAssertions.notNull(createdAt, "createdAt");
    }
}

