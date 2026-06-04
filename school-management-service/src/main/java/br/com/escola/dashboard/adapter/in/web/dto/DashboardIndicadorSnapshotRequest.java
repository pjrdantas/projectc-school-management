package br.com.escola.dashboard.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DashboardIndicadorSnapshotRequest(
        @NotNull UUID publicoDashboardId,
        @NotBlank @Size(max = 100) String codigoIndicador,
        @NotBlank @Size(max = 180) String descricao,
        BigDecimal valorNumeric,
        @Size(max = 180) String valorTexto,
        @NotNull LocalDate referenciaData) {
}
