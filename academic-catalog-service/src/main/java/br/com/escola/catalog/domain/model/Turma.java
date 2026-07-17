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
        id = Validacoes.notNull(id, "id");
        escolaId = Validacoes.notNull(escolaId, "escolaId");
        codigo = Validacoes.notBlank(codigo, "codigo");
        nome = Validacoes.notBlank(nome, "nome");
        capacidade = Validacoes.positive(capacidade, "capacidade");
        periodoLetivoId = Validacoes.notNull(periodoLetivoId, "periodoLetivoId");
        serieId = Validacoes.notNull(serieId, "serieId");
        turnoId = Validacoes.notNull(turnoId, "turnoId");
        createdAt = Validacoes.notNull(createdAt, "createdAt");
    }
}


