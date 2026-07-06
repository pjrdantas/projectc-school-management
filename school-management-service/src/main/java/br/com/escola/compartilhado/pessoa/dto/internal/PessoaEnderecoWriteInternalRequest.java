package br.com.escola.compartilhado.pessoa.dto.internal;

public record PessoaEnderecoWriteInternalRequest(
        String tipoEnderecoCodigo,
        Boolean principal,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {
}
