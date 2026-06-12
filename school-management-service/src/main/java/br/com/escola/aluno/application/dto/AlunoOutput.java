package br.com.escola.aluno.application.dto;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AlunoOutput(
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
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt) {
}
