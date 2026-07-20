package br.com.escola.institutionaltenantservice.application.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record EscolaAdministrada(
        UUID id,
        String nome,
        String codigoInep,
        String cnpj,
        String telefone,
        String email,
        UUID enderecoId,
        boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
