package br.com.escola.planejamento.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlanejamentoBimestralStatusRequest(
        @NotBlank @Size(max = 60) String status) {
}
