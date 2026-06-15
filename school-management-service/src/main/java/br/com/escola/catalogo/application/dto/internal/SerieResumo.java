package br.com.escola.catalogo.application.dto.internal;

import java.util.UUID;

public record SerieResumo(
        UUID id,
        String nome,
        Integer ordem,
        String nivelEnsino
) {
}
