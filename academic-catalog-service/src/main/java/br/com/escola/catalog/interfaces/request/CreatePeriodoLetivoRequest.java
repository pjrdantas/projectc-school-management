package br.com.escola.catalog.interfaces.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePeriodoLetivoRequest(
        @NotBlank @Size(max = 80) String nome,
        @Min(1) int ano,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim) {
}
