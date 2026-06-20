package br.com.escola.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.valueobject.EscolaId;

public record Turma(
        UUID id,
        EscolaId escolaId,
        String codigo,
        String nome,
        int capacidade,
        UUID periodoLetivoId,
        UUID serieId,
        UUID turnoId,
        boolean ativo,
        LocalDateTime createdAt) {

    public Turma {
        id = CatalogAssertions.notNull(id, "id");
        escolaId = CatalogAssertions.notNull(escolaId, "escolaId");
        codigo = CatalogAssertions.notBlank(codigo, "codigo");
        nome = CatalogAssertions.notBlank(nome, "nome");
        capacidade = CatalogAssertions.positive(capacidade, "capacidade");
        periodoLetivoId = CatalogAssertions.notNull(periodoLetivoId, "periodoLetivoId");
        serieId = CatalogAssertions.notNull(serieId, "serieId");
        turnoId = CatalogAssertions.notNull(turnoId, "turnoId");
        createdAt = CatalogAssertions.notNull(createdAt, "createdAt");
    }
}

