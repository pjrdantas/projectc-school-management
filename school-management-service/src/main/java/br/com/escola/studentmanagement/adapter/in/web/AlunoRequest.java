package br.com.escola.studentmanagement.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record AlunoRequest(
        @NotBlank(message = "nomeCompleto é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "cpf é obrigatório")
        @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "cpf deve estar no formato 00000000000 ou 000.000.000-00")
        String cpf,

        @Email(message = "email inválido")
        String email,

        @NotNull(message = "dataNascimento é obrigatória")
        @Past(message = "dataNascimento deve estar no passado")
        LocalDate dataNascimento
) {
}
