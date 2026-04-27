package br.com.escola.studentmanagement.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AlunoOutput(
        Long id,
        String nomeCompleto,
        String cpf,
        String email,
        LocalDate dataNascimento,
        LocalDateTime createdAt) {
}
