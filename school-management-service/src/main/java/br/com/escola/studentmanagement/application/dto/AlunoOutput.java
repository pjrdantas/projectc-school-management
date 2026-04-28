package br.com.escola.studentmanagement.application.dto;

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
        LocalDateTime createdAt) {
}
