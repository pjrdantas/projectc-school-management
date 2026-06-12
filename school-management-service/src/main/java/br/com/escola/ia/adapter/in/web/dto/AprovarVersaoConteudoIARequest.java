package br.com.escola.ia.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AprovarVersaoConteudoIARequest(
        @NotNull @Min(1) Integer numeroVersao,
        Boolean publicarBiblioteca) {
}
