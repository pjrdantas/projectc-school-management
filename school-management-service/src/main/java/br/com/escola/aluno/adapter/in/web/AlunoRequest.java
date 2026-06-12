package br.com.escola.aluno.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.UUID;

public record AlunoRequest(
        @NotBlank(message = "nomeCompleto é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "cpf é obrigatório")
        @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "cpf deve estar no formato 00000000000 ou 000.000.000-00")
        String cpf,

        @Email(message = "email inválido")
        String email,

        @Pattern(regexp = "(^$|^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$|^\\d{10,11}$)", message = "telefone deve estar no formato (99) 99999-9999, (99) 9999-9999 ou somente dígitos")
        String telefone,

        @NotNull(message = "dataNascimento é obrigatória")
        @Past(message = "dataNascimento deve estar no passado")
        LocalDate dataNascimento,

        String rg,
        String orgaoEmissorRg,
        String ufRg,
        String nacionalidade,
        String naturalidade,
        String sexo,
        String nomeSocial,

        @Pattern(regexp = "(^$|^\\d{8}$|^\\d{5}-\\d{3}$)", message = "cep deve estar no formato 00000000 ou 00000-000")
        String cep,

        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String statusAluno,
        UUID escolaId
) {
}
