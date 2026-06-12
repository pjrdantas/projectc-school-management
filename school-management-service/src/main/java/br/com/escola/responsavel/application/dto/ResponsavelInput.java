package br.com.escola.responsavel.application.dto;

import java.util.UUID;

public record ResponsavelInput(
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        String rg,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        UUID escolaId) {
}
