package br.com.escola.catalog.interfaces.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDisciplinaRequest(
        @NotBlank @Size(max = 120) String nome,
        @Min(1) Integer cargaHoraria,
        Boolean ativo) {
}
