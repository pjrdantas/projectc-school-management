package br.com.escola.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.exception.CatalogDomainException;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public record Disciplina(
        UUID id,
        EscolaId escolaId,
        String nome,
        Integer cargaHoraria,
        boolean ativo,
        LocalDateTime createdAt) {

    public Disciplina {
        id = CatalogAssertions.notNull(id, "id");
        escolaId = CatalogAssertions.notNull(escolaId, "escolaId");
        nome = CatalogAssertions.notBlank(nome, "nome");
        createdAt = CatalogAssertions.notNull(createdAt, "createdAt");
        if (cargaHoraria != null && cargaHoraria <= 0) {
            throw new CatalogDomainException("cargaHoraria deve ser positiva");
        }
    }
}

