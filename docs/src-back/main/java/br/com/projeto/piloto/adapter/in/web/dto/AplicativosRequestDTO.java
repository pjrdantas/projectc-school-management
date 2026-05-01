package br.com.projeto.piloto.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AplicativosRequestDTO(
        @NotBlank(message = "O campo 'nome' não pode ser nulo ou vazio") String nome,
        @NotBlank(message = "O campo 'descricao' não pode ser nulo ou vazio") String descricao,
        @NotBlank(message = "O campo 'url' não pode ser nulo ou vazio") String url,
        @NotBlank(message = "O campo 'moduleName' não pode ser nulo ou vazio") String moduleName,
        @NotBlank(message = "O campo 'exposedModule' não pode ser nulo ou vazio") String exposedModule,
        String routePath,      
        String ativo           
) {}
