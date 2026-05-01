package br.com.projeto.piloto.adapter.in.web.dto;

import java.util.Set;

public record AuthUsuarioRequestDTO(
    String login,
    String senha,
    String nome,
    String ativo,
    String email,
    Set<Long> perfisIds   
) {}
