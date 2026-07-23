package br.com.escola.enrollmentdocumentservice.application.dto;

import jakarta.validation.constraints.NotBlank;

public record EscolaOrigemRequest(
        @NotBlank(message = "nomeEscola e obrigatorio")
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
