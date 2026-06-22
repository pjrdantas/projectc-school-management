package br.com.escola.catalog.interfaces.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LinkDisciplinaRequest(
        @NotNull UUID disciplinaId,
        @Min(1) Integer cargaHoraria) {
}
