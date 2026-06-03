package br.com.escola.compartilhado.pessoa.dto;

public record EnderecoDados(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String tipoEnderecoCodigo,
        Boolean principal
) {
}
