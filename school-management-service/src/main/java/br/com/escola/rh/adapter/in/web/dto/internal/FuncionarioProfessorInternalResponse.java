package br.com.escola.rh.adapter.in.web.dto.internal;

import java.util.UUID;

public record FuncionarioProfessorInternalResponse(
        UUID funcionarioId,
        UUID pessoaId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String cargo,
        Boolean ativo,
        Boolean elegivelProfessor) {
}
