package br.com.escola.seguranca.adapter.out.persistence.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.domain.model.UsuarioModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    public UsuarioModel toDomain(UsuarioEntity entity) {
        if (entity == null) return null;

        return UsuarioModel.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .senhaHash(entity.getSenhaHash())
                .ativo(entity.isAtivo())
                .createdAt(entity.getCreatedAt())
                .perfis(
                        entity.getPerfis().stream()
                                .map(PerfilMapper::toDomain)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    public UsuarioEntity toEntity(UsuarioModel model) {
        if (model == null) return null;

        return UsuarioEntity.builder()
                .id(model.getId())
                .username(model.getUsername())
                .nome(model.getNome())
                .email(model.getEmail())
                .senhaHash(model.getSenhaHash())
                .ativo(model.isAtivo())
                .createdAt(model.getCreatedAt())
                .perfis(
                        model.getPerfis().stream()
                                .map(PerfilMapper::toEntity)
                                .collect(Collectors.toSet())
                )
                .build();
    }
}