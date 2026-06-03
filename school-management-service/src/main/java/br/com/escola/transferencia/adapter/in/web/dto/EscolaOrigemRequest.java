package br.com.escola.transferencia.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record EscolaOrigemRequest(
        @NotBlank(message = "nomeEscola é obrigatório")
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
