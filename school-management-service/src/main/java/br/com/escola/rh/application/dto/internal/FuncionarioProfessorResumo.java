package br.com.escola.rh.application.dto.internal;

import java.util.UUID;

public record FuncionarioProfessorResumo(
        UUID funcionarioId,
        UUID pessoaId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String cargo,
        Boolean ativo,
        Boolean elegivelProfessor) {
}
