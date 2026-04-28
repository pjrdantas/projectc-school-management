package br.com.escola.responsavelmanagement.application.dto;

import java.util.UUID;

public record VinculoAlunoResponsavelInput(
        UUID idAluno,
        UUID idResponsavel) {
}
