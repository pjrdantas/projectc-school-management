package br.com.escola.responsavel.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelOutput(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        String rg,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        LocalDateTime createdAt) {
}
