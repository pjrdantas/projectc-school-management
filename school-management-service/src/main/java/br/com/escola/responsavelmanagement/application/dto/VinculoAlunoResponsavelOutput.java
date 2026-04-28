package br.com.escola.responsavelmanagement.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VinculoAlunoResponsavelOutput(
        UUID id,
        UUID idAluno,
        UUID idResponsavel,
        LocalDateTime createdAt) {
}
