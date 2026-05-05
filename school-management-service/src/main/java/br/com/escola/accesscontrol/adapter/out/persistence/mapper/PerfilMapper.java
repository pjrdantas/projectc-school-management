package br.com.escola.accesscontrol.adapter.out.persistence.mapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.escola.accesscontrol.adapter.in.web.dto.PerfilRequest;
import br.com.escola.accesscontrol.adapter.in.web.dto.PerfilResponse;
import br.com.escola.accesscontrol.adapter.out.persistence.entity.PerfilEntity;
import br.com.escola.accesscontrol.domain.model.PerfilModel;
import br.com.escola.accesscontrol.domain.model.PermissaoModel;

public class PerfilMapper {

    // DTO -> DOMAIN
    public static PerfilModel toDomain(PerfilRequest dto, Set<PermissaoModel> permissoes) {
        if (dto == null) return null;

        return PerfilModel.builder()
                .codigo(dto.codigo())
                .nome(dto.nome())
                .descricao(dto.descricao())
                .permissoes(permissoes != null ? permissoes : Collections.emptySet())
                .build();
    }

    // ENTITY -> DOMAIN  ✅ (FALTAVA)
    public static PerfilModel toDomain(PerfilEntity entity) {
        if (entity == null) return null;

        return PerfilModel.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .createdAt(entity.getCreatedAt())
                .permissoes(
                        entity.getPermissoes() != null
                                ? entity.getPermissoes().stream()
                                    .map(PermissaoMapper::toDomain)
                                    .collect(Collectors.toSet())
                                : Collections.emptySet()
                )
                .build();
    }

    // DOMAIN -> ENTITY ✅ (FALTAVA)
    public static PerfilEntity toEntity(PerfilModel model) {
        if (model == null) return null;

        return PerfilEntity.builder()
                .id(model.getId())
                .codigo(model.getCodigo())
                .nome(model.getNome())
                .descricao(model.getDescricao())
                .createdAt(model.getCreatedAt())
                .permissoes(
                        model.getPermissoes() != null
                                ? model.getPermissoes().stream()
                                    .map(PermissaoMapper::toEntity)
                                    .collect(Collectors.toSet())
                                : new HashSet<>()
                )
                .build();
    }

    // DOMAIN -> RESPONSE
    public static PerfilResponse toResponse(PerfilModel domain) {
        if (domain == null) return null;

        return new PerfilResponse(
                domain.getId(),
                domain.getCodigo(),
                domain.getNome(),
                domain.getDescricao(),
                domain.getCreatedAt(),
                domain.getPermissoes() != null
                        ? domain.getPermissoes().stream()
                            .map(PermissaoMapper::toResponse)
                            .collect(Collectors.toSet())
                        : Collections.emptySet()
        );
    }
}