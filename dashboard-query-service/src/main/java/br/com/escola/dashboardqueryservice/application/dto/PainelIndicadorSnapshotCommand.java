package br.com.escola.dashboardqueryservice.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PainelIndicadorSnapshotCommand(
        @NotNull UUID publicoId,
        @NotBlank @Size(max = 100) String codigoIndicador,
        @NotBlank @Size(max = 255) String descricao,
        BigDecimal valorNumeric,
        @Size(max = 255) String valorTexto,
        @Size(max = 255) String escolaNome,
        @NotNull LocalDate referenciaData) {
}
