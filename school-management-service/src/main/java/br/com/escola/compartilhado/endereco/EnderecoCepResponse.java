package br.com.escola.compartilhado.endereco;

public record EnderecoCepResponse(
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String uf,
        String complemento) {
}
