package br.com.projeto.piloto.accesscontrol.adapter.in.web.dto;

import java.util.Set;

public record AuthResponseDTO(
        String token, 
        String refreshToken, 
        String usuario,
        String nome,          
        Set<String> perfis,
        Set<String> permissoes
) {
}
