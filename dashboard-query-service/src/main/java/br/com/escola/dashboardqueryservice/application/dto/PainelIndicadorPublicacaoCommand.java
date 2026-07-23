package br.com.escola.dashboardqueryservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PainelIndicadorPublicacaoCommand(
        @NotNull OrigemIndicador origem,
        @NotNull UUID publicoId,
        @NotNull LocalDate referenciaData,
        @Size(max = 255) String escolaNome,
        @NotEmpty List<@Valid Indicador> indicadores) {

    public record Indicador(
            @NotBlank @Size(max = 100) String codigoIndicador,
            @NotBlank @Size(max = 255) String descricao,
            BigDecimal valorNumeric,
            @Size(max = 255) String valorTexto) {
    }
}
