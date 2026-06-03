package br.com.escola.compartilhado.pessoa.dto;

import java.util.UUID;

public record PessoaCriada(
        UUID pessoaId,
        UUID enderecoId,
        UUID pessoaEnderecoId
) {
}
