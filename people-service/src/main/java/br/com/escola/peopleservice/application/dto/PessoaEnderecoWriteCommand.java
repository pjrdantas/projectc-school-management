package br.com.escola.peopleservice.application.dto;

import java.util.UUID;

public record PessoaEnderecoWriteCommand(
        UUID commandId,
        UUID pessoaId,
        UUID escolaId,
        UUID tipoEnderecoId,
        String tipoEnderecoCodigo,
        boolean principal,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String idempotencyKey,
        String requestedBy) {
}


