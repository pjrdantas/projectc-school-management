package br.com.escola.professorservice.infra.database.mapper;

import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;

public final class ProfessorAlocacaoShadowPersistenceMapper {

    private ProfessorAlocacaoShadowPersistenceMapper() {
    }

    public static ProfessorAlocacaoShadowJpaEntity toEntity(ProfessorAlocacaoResponse response) {
        return new ProfessorAlocacaoShadowJpaEntity(
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
