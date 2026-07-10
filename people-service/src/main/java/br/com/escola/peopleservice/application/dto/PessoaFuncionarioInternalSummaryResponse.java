package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaFuncionarioInternalSummaryResponse(
        UUID funcionarioId,
        UUID pessoaId,
        UUID escolaId,
        String nomeCompleto,
        String cargoDescricao,
        boolean ativo) {
}
