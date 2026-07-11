package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaContatoLocalReadResponse(
        UUID pessoaId,
        UUID escolaId,
        String email,
        String telefone,
        Boolean ativo) {
}
