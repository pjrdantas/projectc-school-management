package br.com.escola.professorservice.infra.database.mapper;

import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;

public final class ProfessorAlocacaoShadowReadMapper {

    private ProfessorAlocacaoShadowReadMapper() {
    }

    public static ProfessorAlocacaoResponse toResponse(
            ProfessorAlocacaoShadowJpaEntity entity,
            ProfessorShadowJpaEntity professor) {
        return new ProfessorAlocacaoResponse(
                entity.getId(),
                entity.getProfessorId(),
                professor.getNomeCompleto(),
                entity.getTurmaDisciplinaId(),
                entity.getTurmaId(),
                entity.getTurmaNome(),
                entity.getDisciplinaId(),
                entity.getDisciplinaNome(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.isAtivo(),
                entity.getCreatedAt());
    }
}
