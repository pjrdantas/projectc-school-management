package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiarioClasseFrequenciaRequest(
        @NotNull UUID idAluno,
        @NotNull LocalDate data,
        Integer dia,
        @NotBlank String status) {
}
