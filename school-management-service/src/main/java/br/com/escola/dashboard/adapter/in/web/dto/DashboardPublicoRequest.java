package br.com.escola.dashboard.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DashboardPublicoRequest(
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 120) String descricao) {
}
