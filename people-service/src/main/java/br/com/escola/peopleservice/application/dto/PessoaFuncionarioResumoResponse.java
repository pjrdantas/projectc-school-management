package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaFuncionarioResumoResponse(
        UUID funcionarioId,
        UUID pessoaId,
        UUID escolaId,
        String nomeCompleto,
        String cargoDescricao,
        boolean ativo) {
}


