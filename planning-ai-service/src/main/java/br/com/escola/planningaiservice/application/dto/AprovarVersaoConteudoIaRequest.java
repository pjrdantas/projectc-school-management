package br.com.escola.planningaiservice.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AprovarVersaoConteudoIaRequest(
        @NotNull @Min(1) Integer numeroVersao,
        Boolean publicarBiblioteca) {
}
