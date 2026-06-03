package br.com.escola.seguranca.adapter.out.persistence.mapper;

import br.com.escola.seguranca.adapter.in.web.dto.PermissaoRequest;
import br.com.escola.seguranca.adapter.in.web.dto.PermissaoResponse;
import br.com.escola.seguranca.adapter.out.persistence.entity.PermissaoEntity;
import br.com.escola.seguranca.domain.model.PermissaoModel;

public class PermissaoMapper {

    // DTO -> Domain
    public static PermissaoModel toDomain(PermissaoRequest dto) {
        if (dto == null) return null;

        return PermissaoModel.builder()
                .codigo(dto.codigo())
                .descricao(dto.descricao())
                .build();
    }

    // Entity -> Domain
    public static PermissaoModel toDomain(PermissaoEntity entity) {
        if (entity == null) return null;

        return PermissaoModel.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // Domain -> Entity
    public static PermissaoEntity toEntity(PermissaoModel domain) {
        if (domain == null) return null;

        return PermissaoEntity.builder()
                .id(domain.getId())
                .codigo(domain.getCodigo())
                .descricao(domain.getDescricao())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    // Domain -> Response DTO
    public static PermissaoResponse toResponse(PermissaoModel domain) {
        if (domain == null) return null;

        return new PermissaoResponse(
                domain.getId(),
                domain.getCodigo(),
                domain.getDescricao(),
                domain.getCreatedAt()
        );
    }
}