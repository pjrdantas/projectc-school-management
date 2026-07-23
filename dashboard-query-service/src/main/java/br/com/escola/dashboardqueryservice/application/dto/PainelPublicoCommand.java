package br.com.escola.dashboardqueryservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PainelPublicoCommand(
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 120) String descricao) {
}
