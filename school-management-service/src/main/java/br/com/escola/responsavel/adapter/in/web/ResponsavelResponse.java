package br.com.escola.responsavel.adapter.in.web;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelResponse(
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
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt) {
}
