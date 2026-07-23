package br.com.escola.identityaccessservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SelecionarEscolaAtivaRequest(
        @NotNull(message = "escolaId e obrigatorio")
        UUID escolaId) {
}
