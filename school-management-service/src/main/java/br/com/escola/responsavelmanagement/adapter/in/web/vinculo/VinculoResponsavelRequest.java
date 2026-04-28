package br.com.escola.responsavelmanagement.adapter.in.web.vinculo;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record VinculoResponsavelRequest(
        @NotNull(message = "idResponsavel é obrigatório")
        UUID idResponsavel) {
}
