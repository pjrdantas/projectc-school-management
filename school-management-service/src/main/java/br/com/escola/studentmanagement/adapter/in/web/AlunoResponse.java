package br.com.escola.studentmanagement.adapter.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AlunoResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDate dataNascimento,
        LocalDateTime createdAt
) {
}
