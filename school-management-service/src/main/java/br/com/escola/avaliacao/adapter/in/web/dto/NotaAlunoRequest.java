package br.com.escola.avaliacao.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record NotaAlunoRequest(
        @NotNull UUID matriculaId,
        @NotNull @DecimalMin("0.00") BigDecimal nota,
        String observacao) {
}
