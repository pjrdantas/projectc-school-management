package br.com.escola.responsavel.adapter.in.web.vinculo;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelVinculadoResponse(
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
        String parentesco,
        Boolean responsavelFinanceiro,
        Boolean responsavelPedagogico,
        Boolean autorizadoRetirar,
        LocalDateTime createdAt) {
}
