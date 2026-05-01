package br.com.projeto.piloto.adapter.in.web.dto;

import java.util.Set;

public record AuthPerfilResponseDTO(
        Long id,
        String nmPerfil,
        Set<AuthPermissaoResponseDTO> permissoes 
) {}
