package br.com.escola.ia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarVersaoConteudoIARequest(
        @NotBlank String conteudo,
        String motivoAlteracao) {
}
