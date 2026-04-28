package br.com.escola.responsavelmanagement.adapter.in.web.vinculo;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelVinculadoResponse(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDateTime createdAt) {
}
