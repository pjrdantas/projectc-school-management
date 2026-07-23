package br.com.escola.catalog.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTurnoRequest(@NotBlank @Size(max = 40) String codigo, @NotBlank @Size(max = 120) String descricao) {
}
