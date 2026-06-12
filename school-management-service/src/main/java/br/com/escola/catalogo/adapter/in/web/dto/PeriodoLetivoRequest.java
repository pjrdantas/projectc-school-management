package br.com.escola.catalogo.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PeriodoLetivoRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        Integer ano,

        @NotNull(message = "dataInicio é obrigatória")
        LocalDate dataInicio,

        @NotNull(message = "dataFim é obrigatória")
        LocalDate dataFim,

        UUID escolaId
) {
}
