package br.com.escola.institucional.application.dto;

import java.util.Set;
import java.util.UUID;

public record EscolaContexto(
        UUID escolaId,
        String escolaNome,
        UUID usuarioId,
        Set<String> perfis,
        Set<String> permissoes
) {
}
