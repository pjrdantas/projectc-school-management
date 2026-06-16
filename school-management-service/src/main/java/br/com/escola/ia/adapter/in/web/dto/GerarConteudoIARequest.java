package br.com.escola.ia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GerarConteudoIARequest(
        @NotBlank @Size(max = 4000) String promptProfessor,
        @NotBlank @Size(max = 60) String tipoConteudo,
        @Size(max = 180) String titulo,
        Boolean reutilizavel) {
}
