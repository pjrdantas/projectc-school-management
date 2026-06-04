package br.com.escola.avaliacao.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull UUID professorTurmaDisciplinaId,
        @NotBlank @Size(max = 150) String titulo,
        String descricao,
        LocalDate dataAplicacao,
        @NotNull @DecimalMin("0.01") BigDecimal valorMaximo,
        @NotNull @DecimalMin("0.01") BigDecimal peso,
        @NotBlank @Size(max = 50) String tipoAvaliacao) {
}
