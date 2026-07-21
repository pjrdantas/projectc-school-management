package br.com.escola.responsiblesservice.application.dto;

public record AtualizarResponsavelCommand(
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
        String uf) {
}
