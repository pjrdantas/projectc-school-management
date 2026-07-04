package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaEnderecoLocalReadResponse(
        UUID pessoaEnderecoId,
        UUID pessoaId,
        UUID enderecoId,
        UUID tipoEnderecoId,
        String tipoEnderecoCodigo,
        String tipoEnderecoDescricao,
        boolean principal,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {
}
