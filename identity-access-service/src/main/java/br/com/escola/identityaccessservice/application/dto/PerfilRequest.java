package br.com.escola.identityaccessservice.application.dto;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
        @NotBlank(message = "codigo e obrigatorio")
        @Size(max = 50, message = "codigo deve ter no maximo 50 caracteres")
        String codigo,
        @JsonAlias("nmPerfil")
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120, message = "nome deve ter no maximo 120 caracteres")
        String nome,
        @Size(max = 255, message = "descricao deve ter no maximo 255 caracteres")
        String descricao,
        @JsonAlias("permissoesIds")
        Set<UUID> permissaoIds) {
}
