package br.com.escola.responsavel.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VinculoAlunoResponsavelOutput(
        UUID id,
        UUID idAluno,
        UUID idResponsavel,
        String parentesco,
        Boolean responsavelFinanceiro,
        Boolean responsavelPedagogico,
        Boolean autorizadoRetirar,
        LocalDateTime createdAt) {
}
