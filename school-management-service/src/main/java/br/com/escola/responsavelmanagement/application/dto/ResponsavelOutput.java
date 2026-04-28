package br.com.escola.responsavelmanagement.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelOutput(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDateTime createdAt) {
}
