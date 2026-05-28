package br.com.escola.enrollment.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record MatriculaStatusRequest(
        @NotBlank(message = "status é obrigatório")
        String status,
        String justificativa
) {
}
