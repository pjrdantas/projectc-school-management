package br.com.escola.catalog.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.exception.DominioException;
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
        id = Validacoes.notNull(id, "id");
        escolaId = Validacoes.notNull(escolaId, "escolaId");
        nome = Validacoes.notBlank(nome, "nome");
        ano = Validacoes.positive(ano, "ano");
        dataInicio = Validacoes.notNull(dataInicio, "dataInicio");
        dataFim = Validacoes.notNull(dataFim, "dataFim");
        createdAt = Validacoes.notNull(createdAt, "createdAt");
        if (dataFim.isBefore(dataInicio)) {
            throw new DominioException("dataFim nao pode ser anterior a dataInicio");
        }
    }
}


