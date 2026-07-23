package br.com.escola.professorservice.infra.database.mapper;

import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;

public final class AlocacaoPersistenciaMapper {

    private AlocacaoPersistenciaMapper() {
    }

    public static AlocacaoJpaEntity toEntity(AlocacaoResponse response) {
        return new AlocacaoJpaEntity(
                response.id(),
                response.professorId(),
                response.turmaDisciplinaId(),
                response.turmaId(),
                response.turmaNome(),
                response.disciplinaId(),
                response.disciplinaNome(),
                response.dataInicio(),
                response.dataFim(),
                Boolean.TRUE.equals(response.ativo()),
                response.createdAt());
    }
}

