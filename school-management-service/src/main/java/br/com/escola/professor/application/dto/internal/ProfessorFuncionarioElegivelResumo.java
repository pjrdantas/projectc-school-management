package br.com.escola.professor.application.dto.internal;

import java.util.UUID;

public record ProfessorFuncionarioElegivelResumo(
        UUID funcionarioId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String cargo,
        Boolean ativo) {
}
