package br.com.projeto.piloto.adapter.out.jpa.mapper;

import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoResponseDTO;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPermissao;
import br.com.projeto.piloto.domain.model.AuthPermissaoModel;

public class AuthPermissaoMapper {


    public static AuthPermissaoModel toDomain(AuthPermissaoRequestDTO dto) {
        if (dto == null) return null;

        return AuthPermissaoModel.builder()
                .nmPermissao(dto.nmPermissao())
                .build();
    }

    public static AuthPermissaoModel toDomain(AuthPermissao entity) {
        if (entity == null) return null;

        return AuthPermissaoModel.builder()
                .id(entity.getId())
                .nmPermissao(entity.getNmPermissao())
                .build();
    }

    public static AuthPermissao toEntity(AuthPermissaoModel domain) {
        if (domain == null) return null;

        AuthPermissao entity = new AuthPermissao();
        entity.setId(domain.getId());
        entity.setNmPermissao(domain.getNmPermissao());
        return entity;
    }

    public static AuthPermissaoResponseDTO toResponse(AuthPermissaoModel domain) {
        if (domain == null) return null;

        return new AuthPermissaoResponseDTO(
                domain.getId(),
                domain.getNmPermissao()
        );
    }
}