package br.com.escola.compartilhado.pessoa.dto.internal;

import java.util.UUID;

public record PessoaEnderecoResumo(
        UUID pessoaEnderecoId,
        UUID enderecoId,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String tipoEnderecoCodigo,
        boolean principal) {
}
