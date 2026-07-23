package br.com.escola.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalog.domain.exception.DominioException;
import br.com.escola.catalog.domain.valueobject.EscolaId;

public record TurmaDisciplina(
        UUID id,
        EscolaId escolaId,
        UUID turmaId,
        UUID disciplinaId,
        Integer cargaHoraria,
        LocalDateTime createdAt) {

    public TurmaDisciplina {
        id = Validacoes.notNull(id, "id");
        escolaId = Validacoes.notNull(escolaId, "escolaId");
        turmaId = Validacoes.notNull(turmaId, "turmaId");
        disciplinaId = Validacoes.notNull(disciplinaId, "disciplinaId");
        createdAt = Validacoes.notNull(createdAt, "createdAt");
        if (cargaHoraria != null && cargaHoraria <= 0) {
            throw new DominioException("cargaHoraria deve ser positiva");
        }
    }
}


