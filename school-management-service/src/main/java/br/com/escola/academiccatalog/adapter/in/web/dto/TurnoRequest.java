package br.com.escola.academiccatalog.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TurnoRequest(
        @NotBlank(message = "codigo é obrigatório")
        String codigo,

        @NotBlank(message = "descricao é obrigatória")
        String descricao) {
}
