package br.com.escola.responsavel.application.dto.consulta;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelResumoOutput(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDateTime createdAt) {
}
