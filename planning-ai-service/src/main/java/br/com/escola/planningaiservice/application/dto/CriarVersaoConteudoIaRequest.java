package br.com.escola.planningaiservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarVersaoConteudoIaRequest(
        @NotBlank @Size(max = 20000) String conteudo,
        @Size(max = 2000) String motivoAlteracao) {
}
