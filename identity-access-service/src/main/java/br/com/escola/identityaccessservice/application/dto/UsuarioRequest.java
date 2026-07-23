package br.com.escola.identityaccessservice.application.dto;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @JsonAlias("login")
        @NotBlank(message = "username e obrigatorio")
        @Size(max = 80, message = "username deve ter no maximo 80 caracteres")
        String username,
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 150, message = "nome deve ter no maximo 150 caracteres")
        String nome,
        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email invalido")
        @Size(max = 150, message = "email deve ter no maximo 150 caracteres")
        String email,
        @JsonAlias("senha")
        @NotBlank(message = "senhaHash e obrigatorio")
        @Size(max = 255, message = "senhaHash deve ter no maximo 255 caracteres")
        String senhaHash,
        Boolean ativo,
        @JsonAlias("perfisIds")
        Set<UUID> perfilIds,
        UUID escolaId) {
}
