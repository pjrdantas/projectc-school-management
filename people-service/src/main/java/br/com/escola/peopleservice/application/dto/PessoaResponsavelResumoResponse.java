package br.com.escola.peopleservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PessoaResponsavelResumoResponse(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDateTime createdAt) {
}
