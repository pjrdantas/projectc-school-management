package br.com.escola.bff.interfaces.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TurmaDisciplinaRequest(
        @NotNull(message = "disciplinaId é obrigatório")
        UUID disciplinaId,

        @Min(value = 1, message = "cargaHoraria deve ser maior que zero")
        Integer cargaHoraria
) {}
