package br.com.escola.responsavelmanagement.application.dto;

public record ResponsavelInput(
        String nomeCompleto,
        String cpf,
        String email,
        String telefone) {
}
