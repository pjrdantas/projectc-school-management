package br.com.escola.responsavel.application.dto;

import java.util.UUID;

public record VinculoAlunoResponsavelInput(
        UUID idAluno,
        UUID idResponsavel,
        String parentesco,
        Boolean responsavelFinanceiro,
        Boolean responsavelPedagogico,
        Boolean autorizadoRetirar) {
}
