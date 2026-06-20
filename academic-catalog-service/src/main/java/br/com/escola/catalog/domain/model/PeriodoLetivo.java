package br.com.escola.catalog.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.exception.CatalogDomainException;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public record PeriodoLetivo(
        UUID id,
        EscolaId escolaId,
        String nome,
        int ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativo,
        LocalDateTime createdAt) {

    public PeriodoLetivo {
        id = CatalogAssertions.notNull(id, "id");
        escolaId = CatalogAssertions.notNull(escolaId, "escolaId");
        nome = CatalogAssertions.notBlank(nome, "nome");
        ano = CatalogAssertions.positive(ano, "ano");
        dataInicio = CatalogAssertions.notNull(dataInicio, "dataInicio");
        dataFim = CatalogAssertions.notNull(dataFim, "dataFim");
        createdAt = CatalogAssertions.notNull(createdAt, "createdAt");
        if (dataFim.isBefore(dataInicio)) {
            throw new CatalogDomainException("dataFim nao pode ser anterior a dataInicio");
        }
    }
}

