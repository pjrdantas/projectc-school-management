package br.com.escola.seguranca.adapter.in.web.dto.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SelecionarEscolaAtivaRequest(
        @NotNull(message = "escolaId é obrigatório")
        UUID escolaId
) {
}
