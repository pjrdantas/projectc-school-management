package br.com.escola.professor.adapter.in.web.dto.internal;

import java.util.UUID;

public record ProfessorFuncionarioElegivelInternalResponse(
        UUID funcionarioId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String cargo,
        Boolean ativo) {
}
