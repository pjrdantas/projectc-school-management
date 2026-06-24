package br.com.escola.professorservice.application.dto;

import java.util.UUID;

public record FuncionarioElegivelResponse(
        UUID funcionarioId,
        UUID pessoaId,
        String nomeCompleto,
        UUID escolaId,
        String escolaNome,
        String cargo,
        Boolean ativo,
        Boolean elegivelProfessor) {
}
