package br.com.escola.studentmanagement.application.dto;

import java.time.LocalDate;

public record AlunoInput(
        String nomeCompleto,
        String cpf,
        String email,
        LocalDate dataNascimento) {
}
