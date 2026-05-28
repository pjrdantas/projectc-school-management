package br.com.escola.shared.person.dto;

import java.time.LocalDate;

public record PessoaDados(
        String nomeCompleto,
        String cpf,
        String rg,
        String orgaoEmissorRg,
        String ufRg,
        String email,
        String telefone,
        LocalDate dataNascimento,
        String sexo,
        String nomeSocial,
        String nacionalidade,
        String naturalidade,
        Boolean ativo
) {
}
