package br.com.escola.responsavel.adapter.in.web.vinculo;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record VinculoResponsavelRequest(
        @NotNull(message = "idResponsavel é obrigatório")
        UUID idResponsavel,
        String parentesco,
        Boolean responsavelFinanceiro,
        Boolean responsavelPedagogico,
        Boolean autorizadoRetirar) {
}
