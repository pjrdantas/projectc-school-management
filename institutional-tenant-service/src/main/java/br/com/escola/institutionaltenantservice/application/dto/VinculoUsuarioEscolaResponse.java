package br.com.escola.institutionaltenantservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.institutionaltenantservice.application.model.VinculoUsuarioEscola;

public record VinculoUsuarioEscolaResponse(
        UUID id,
        UUID usuarioId,
        UUID escolaId,
        LocalDateTime createdAt) {

    public static VinculoUsuarioEscolaResponse from(VinculoUsuarioEscola vinculo) {
        return new VinculoUsuarioEscolaResponse(
                vinculo.id(), vinculo.usuarioId(), vinculo.escolaId(), vinculo.createdAt());
    }
}
