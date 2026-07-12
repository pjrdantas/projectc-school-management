package br.com.escola.transferencia.application.dto.internal;

public record EscolaOrigemSolicitacao(
        String nomeEscola,
        String codigoInep,
        String cnpj,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf) {
}
