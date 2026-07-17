package br.com.escola.catalog.domain.model;

import java.util.Locale;
import java.util.UUID;

public record NivelEnsino(
        UUID id,
        String codigo,
        String descricao) {

    public NivelEnsino {
        id = Validacoes.notNull(id, "id");
        codigo = Validacoes.notBlank(codigo, "codigo").toUpperCase(Locale.ROOT);
        descricao = Validacoes.notBlank(descricao, "descricao");
    }
}

