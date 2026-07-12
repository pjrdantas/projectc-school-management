package br.com.escola.compartilhado.pessoa.dto.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record PessoaResponsavelResumoInternalResponse(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDateTime createdAt) {
}
