package br.com.escola.catalogo.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TurmaRequest(
        @NotBlank(message = "codigo é obrigatório")
        @Size(max = 20, message = "codigo deve ter no máximo 20 caracteres")
        String codigo,

        @NotBlank(message = "nome é obrigatório")
        @Size(max = 120, message = "nome deve ter no máximo 120 caracteres")
        String nome,

        @NotNull(message = "capacidade é obrigatória")
        @Min(value = 1, message = "capacidade deve ser maior que zero")
        Integer capacidade,

        @NotNull(message = "periodoLetivoId é obrigatório")
        UUID periodoLetivoId,

        @NotNull(message = "serieId é obrigatório")
        UUID serieId,

        @Size(max = 40, message = "turno deve ter no máximo 40 caracteres")
        String turno,

        @Size(max = 20, message = "status deve ter no máximo 20 caracteres")
        String status
) {
}
