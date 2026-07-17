package br.com.escola.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.exception.DominioException;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public record Disciplina(
        UUID id,
        EscolaId escolaId,
        String nome,
        Integer cargaHoraria,
        boolean ativo,
        LocalDateTime createdAt) {

    public Disciplina {
        id = Validacoes.notNull(id, "id");
        escolaId = Validacoes.notNull(escolaId, "escolaId");
        nome = Validacoes.notBlank(nome, "nome");
        createdAt = Validacoes.notNull(createdAt, "createdAt");
        if (cargaHoraria != null && cargaHoraria <= 0) {
            throw new DominioException("cargaHoraria deve ser positiva");
        }
    }
}


