package br.com.escola.responsavelmanagement.adapter.in.web;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelResponse(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDateTime createdAt) {
}
