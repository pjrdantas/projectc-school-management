package br.com.escola.planejamento.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlanejamentoBimestralAulaRequest(
        @NotNull @Min(1) Integer numeroAula,
        @NotBlank @Size(max = 180) String temaAula,
        String objetivoAula,
        String conteudoPrevisto,
        String metodologia,
        String recursos,
        String atividadePrevista,
        String observacao) {
}
