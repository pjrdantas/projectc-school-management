package br.com.projeto.piloto.adapter.out.jpa.mapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.projeto.piloto.adapter.in.web.dto.AuthPerfilRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthPerfilResponseDTO;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPerfil;
import br.com.projeto.piloto.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.domain.model.AuthPermissaoModel;

public class AuthPerfilMapper {

    public static AuthPerfilModel toDomain(AuthPerfilRequestDTO dto, Set<AuthPermissaoModel> permissoes) {
        if (dto == null) return null;
        return AuthPerfilModel.builder()
                .nmPerfil(dto.nmPerfil())
                .permissoes(permissoes != null ? permissoes : Collections.emptySet())
                .build();
    }

    public static AuthPerfilModel toDomain(AuthPerfil entity) {
        if (entity == null) return null;
        
        Set<AuthPermissaoModel> permissoes = entity.getPermissoes() == null ? Collections.emptySet() : 
            entity.getPermissoes().stream()
                .map(AuthPermissaoMapper::toDomain)
                .collect(Collectors.toSet());

        return AuthPerfilModel.builder()
                .id(entity.getId())
                .nmPerfil(entity.getNmPerfil())
                .permissoes(permissoes)
                .build();
    }

    public static AuthPerfil toEntity(AuthPerfilModel domain) {
        if (domain == null) return null;
        AuthPerfil entity = new AuthPerfil();
        entity.setId(domain.getId());
        entity.setNmPerfil(domain.getNmPerfil());
        
        entity.setPermissoes(domain.getPermissoes() == null ? new HashSet<>() : 
            domain.getPermissoes().stream()
                .map(AuthPermissaoMapper::toEntity)
                .collect(Collectors.toSet()));
                
        return entity;
    }

    public static AuthPerfilResponseDTO toResponse(AuthPerfilModel domain) {
        if (domain == null) return null;
        
        return new AuthPerfilResponseDTO(
                domain.getId(),
                domain.getNmPerfil(),
                domain.getPermissoes() != null ? 
                    domain.getPermissoes().stream()
                        .map(AuthPermissaoMapper::toResponse) 
                        .collect(Collectors.toSet()) : Collections.emptySet()
        );
    }
}
