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
        LocalDateTime createdAt
) {
}
