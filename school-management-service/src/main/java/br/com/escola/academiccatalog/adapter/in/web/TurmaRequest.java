package br.com.escola.academiccatalog.adapter.in.web;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TurmaRequest(
        @NotBlank(message = "codigo é obrigatório")
        String codigo,

        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotNull(message = "capacidade é obrigatória")
        @Min(value = 1, message = "capacidade deve ser maior que zero")
        Integer capacidade,

        @NotNull(message = "periodoLetivoId é obrigatório")
        UUID periodoLetivoId
) {
}
