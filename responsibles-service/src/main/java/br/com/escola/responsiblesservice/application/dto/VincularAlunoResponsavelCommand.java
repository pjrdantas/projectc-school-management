package br.com.escola.responsiblesservice.application.dto;

import java.util.UUID;

public record VincularAlunoResponsavelCommand(
        UUID responsavelId,
        String parentesco,
        Boolean responsavelFinanceiro,
        Boolean responsavelPedagogico,
        Boolean autorizadoRetirar) {
}
