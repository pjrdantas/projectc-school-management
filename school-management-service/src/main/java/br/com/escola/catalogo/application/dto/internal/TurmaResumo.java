package br.com.escola.catalogo.application.dto.internal;

import java.util.UUID;

public record TurmaResumo(
        UUID id,
        String codigo,
        String nome,
        Integer capacidade,
        UUID periodoLetivoId,
        String periodoLetivoNome,
        UUID serieId,
        String serieNome,
        String turno,
        boolean ativo
) {
}
