package br.com.escola.shared.person.dto;

import java.util.UUID;

public record PessoaCriada(
        UUID pessoaId,
        UUID enderecoId,
        UUID pessoaEnderecoId
) {
}
