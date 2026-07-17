package br.com.escola.professorservice.infra.database.mapper;

import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;

public final class AlocacaoReadMapper {

    private AlocacaoReadMapper() {
    }

    public static AlocacaoResponse toResponse(
            AlocacaoJpaEntity entity,
            CadastroJpaEntity professor) {
        return new AlocacaoResponse(
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

