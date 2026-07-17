package br.com.escola.professorservice.infra.database.mapper;

import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;

public final class PersistenciaMapper {

    private PersistenciaMapper() {
    }

    public static CadastroJpaEntity toEntity(ResumoResponse response) {
        return new CadastroJpaEntity(
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

    public static ResumoResponse toResponse(CadastroJpaEntity entity) {
        return new ResumoResponse(
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

