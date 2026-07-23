package br.com.escola.peopleservice.application.model;

import java.time.LocalDate;
import java.util.UUID;

public record AlunoNovo(
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
        String escolaNome) {
}
