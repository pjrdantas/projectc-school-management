package br.com.escola.professorservice.infra.database.mapper;

import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;

public final class ProfessorShadowPersistenceMapper {

    private ProfessorShadowPersistenceMapper() {
    }

    public static ProfessorShadowJpaEntity toEntity(ProfessorResumoResponse response) {
        return new ProfessorShadowJpaEntity(
                response.id(),
                response.pessoaId(),
                response.nomeCompleto(),
                response.escolaId(),
                response.escolaNome(),
                response.registroProfissional(),
                response.formacao(),
                response.ativo(),
                response.createdAt(),
                response.updatedAt(),
                null);
    }

    public static ProfessorResumoResponse toResponse(ProfessorShadowJpaEntity entity) {
        return new ProfessorResumoResponse(
                entity.getId(),
                entity.getPessoaId(),
                entity.getNomeCompleto(),
                entity.getEscolaId(),
                entity.getEscolaNome(),
                entity.getRegistroProfissional(),
                entity.getFormacao(),
                entity.getAtivo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
