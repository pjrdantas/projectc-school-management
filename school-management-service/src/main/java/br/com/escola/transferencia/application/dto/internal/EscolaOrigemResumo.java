package br.com.escola.transferencia.application.dto.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record EscolaOrigemResumo(
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
