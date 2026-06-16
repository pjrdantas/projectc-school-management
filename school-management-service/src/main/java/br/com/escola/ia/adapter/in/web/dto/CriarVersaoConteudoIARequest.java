package br.com.escola.ia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarVersaoConteudoIARequest(
        @NotBlank @Size(max = 20000) String conteudo,
        @Size(max = 2000) String motivoAlteracao) {
}
