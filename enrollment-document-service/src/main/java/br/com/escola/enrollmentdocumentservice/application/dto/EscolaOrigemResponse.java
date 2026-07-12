package br.com.escola.enrollmentdocumentservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EscolaOrigemResponse(
        UUID id,
        String nomeEscola,
        String codigoInep,
        String cnpj,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        LocalDateTime createdAt) {
}
