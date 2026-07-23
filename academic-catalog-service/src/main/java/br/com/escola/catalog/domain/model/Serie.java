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
        id = Validacoes.notNull(id, "id");
        escolaId = Validacoes.notNull(escolaId, "escolaId");
        nome = Validacoes.notBlank(nome, "nome");
        ordem = Validacoes.positive(ordem, "ordem");
        nivelEnsinoId = Validacoes.notNull(nivelEnsinoId, "nivelEnsinoId");
        createdAt = Validacoes.notNull(createdAt, "createdAt");
    }
}


