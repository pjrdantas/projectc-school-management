package br.com.projeto.piloto.accesscontrol.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthPermissaoRequestDTO(@NotBlank(message = "O campo 'nmPermissao' não pode ser nulo ou vazio") String nmPermissao
        
) { }
