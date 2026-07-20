package br.com.escola.institutionaltenantservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.institutionaltenantservice.application.model.EscolaAdministrada;

public record EscolaResponse(
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

    public static EscolaResponse from(EscolaAdministrada escola) {
        return new EscolaResponse(
                escola.id(), escola.nome(), escola.codigoInep(), escola.cnpj(),
                escola.telefone(), escola.email(), escola.enderecoId(), escola.ativo(),
                escola.createdAt(), escola.updatedAt());
    }
}
