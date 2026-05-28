package br.com.escola.studentmanagement.adapter.in.web;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AlunoResponse(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDate dataNascimento,
        String rg,
        String orgaoEmissorRg,
        String ufRg,
        String nacionalidade,
        String naturalidade,
        String sexo,
        String nomeSocial,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String statusAluno,
        LocalDateTime createdAt
) {
}
