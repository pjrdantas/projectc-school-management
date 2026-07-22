package br.com.escola.catalog.interfaces.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSerieRequest(
        @NotBlank @Size(max = 80) String nome,
        @Min(1) int ordem,
        @NotNull UUID nivelEnsinoId) {
}
