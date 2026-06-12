package br.com.escola.professor.adapter.in.web.dto;

import java.util.UUID;

public record ProfessorFuncionarioElegivelResponse(
        UUID funcionarioId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String cargo,
        Boolean ativo) {
}
