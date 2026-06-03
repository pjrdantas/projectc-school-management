package br.com.escola.catalogo.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SerieRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotNull(message = "ordem é obrigatória")
        @Min(value = 0, message = "ordem deve ser maior ou igual a zero")
        Integer ordem,

        String nivelEnsino) {
}
