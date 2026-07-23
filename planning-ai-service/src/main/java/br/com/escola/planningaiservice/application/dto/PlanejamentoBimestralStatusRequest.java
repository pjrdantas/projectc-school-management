package br.com.escola.planningaiservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlanejamentoBimestralStatusRequest(@NotBlank @Size(max = 60) String status) {
}
