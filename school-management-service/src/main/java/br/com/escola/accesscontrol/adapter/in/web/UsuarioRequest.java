package br.com.escola.accesscontrol.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "username é obrigatório")
        @Size(max = 80, message = "username deve ter no máximo 80 caracteres")
        String username,

        @NotBlank(message = "nome é obrigatório")
        @Size(max = 150, message = "nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        @Size(max = 150, message = "email deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "senhaHash é obrigatório")
        @Size(max = 255, message = "senhaHash deve ter no máximo 255 caracteres")
        String senhaHash,

        Boolean ativo
) {}
