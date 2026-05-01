package br.com.projeto.piloto.adapter.in.web.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public record AuthPerfilRequestDTO(
        @NotBlank(message = "O campo 'nmPerfil' não pode ser nulo ou vazio") String nmPerfil, 
        Set<Long> permissoesIds
) { }
