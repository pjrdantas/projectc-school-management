package br.com.escola.catalog.domain.model;

import java.util.UUID;

public record Turno(
        UUID id,
        String codigo,
        String descricao) {

    public Turno {
        id = Validacoes.notNull(id, "id");
        codigo = Validacoes.notBlank(codigo, "codigo").toUpperCase(java.util.Locale.ROOT);
        descricao = Validacoes.notBlank(descricao, "descricao");
    }
}


