package br.com.escola.planejamento.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlanejamentoBimestralAvaliacaoRequest(
        @NotBlank @Size(max = 180) String titulo,
        String descricao,
        LocalDate dataPrevista,
        @NotNull @DecimalMin("0.01") BigDecimal peso,
        @DecimalMin("0.01") BigDecimal valorMaximo,
        @NotBlank @Size(max = 50) String tipoAvaliacao,
        String conteudoCobrado,
        String orientacaoAplicacao) {
}
