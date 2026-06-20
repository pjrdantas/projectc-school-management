package br.com.escola.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.exception.CatalogDomainException;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public record TurmaDisciplina(
        UUID id,
        EscolaId escolaId,
        UUID turmaId,
        UUID disciplinaId,
        Integer cargaHoraria,
        LocalDateTime createdAt) {

    public TurmaDisciplina {
        id = CatalogAssertions.notNull(id, "id");
        escolaId = CatalogAssertions.notNull(escolaId, "escolaId");
        turmaId = CatalogAssertions.notNull(turmaId, "turmaId");
        disciplinaId = CatalogAssertions.notNull(disciplinaId, "disciplinaId");
        createdAt = CatalogAssertions.notNull(createdAt, "createdAt");
        if (cargaHoraria != null && cargaHoraria <= 0) {
            throw new CatalogDomainException("cargaHoraria deve ser positiva");
        }
    }
}

