package br.com.escola.identityaccessservice.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissaoRequest(
        @JsonAlias("nmPermissao")
        @NotBlank(message = "codigo e obrigatorio")
        @Size(max = 80, message = "codigo deve ter no maximo 80 caracteres")
        String codigo,
        @Size(max = 255, message = "descricao deve ter no maximo 255 caracteres")
        String descricao) {
}
