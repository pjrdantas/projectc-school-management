package br.com.escola.bff.application.dto;

import java.util.UUID;

public record SerieCreateCommand(
        String nome,
        Integer ordem,
        String nivelEnsino,
        UUID escolaId
) {
}
