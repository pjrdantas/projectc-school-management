package br.com.escola.dashboard.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DashboardConfiguracaoRequest(
        @NotNull UUID publicoDashboardId,
        @NotBlank @Size(max = 80) String codigo,
        @NotBlank @Size(max = 150) String nome,
        String descricao,
        Boolean ativo) {
}
