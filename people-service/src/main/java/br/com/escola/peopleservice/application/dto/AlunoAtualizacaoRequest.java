package br.com.escola.peopleservice.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlunoAtualizacaoRequest(
        @NotBlank @Size(max = 150) String nomeCompleto,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}") String cpf,
        @Email @Size(max = 150) String email,
        @Pattern(regexp = "(^$|^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$|^\\d{10,11}$)") String telefone,
        @NotNull @Past LocalDate dataNascimento,
        @Size(max = 20) String rg,
        @Size(max = 20) String orgaoEmissorRg,
        @Size(max = 2) String ufRg,
        @Size(max = 80) String nacionalidade,
        @Size(max = 100) String naturalidade,
        @Size(max = 20) String sexo,
        @Size(max = 150) String nomeSocial,
        @Pattern(regexp = "(^$|^\\d{8}$|^\\d{5}-\\d{3}$)") String cep,
        @Size(max = 150) String logradouro,
        @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @Size(max = 100) String bairro,
        @Size(max = 100) String cidade,
        @Size(max = 2) String uf,
        @Size(max = 40) String statusAluno,
        UUID escolaId) {
}
