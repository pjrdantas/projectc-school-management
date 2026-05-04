package br.com.escola.academiccatalog.adapter.in.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PeriodoLetivoRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotNull(message = "dataInicio é obrigatória")
        LocalDate dataInicio,

        @NotNull(message = "dataFim é obrigatória")
        LocalDate dataFim
) {
}
