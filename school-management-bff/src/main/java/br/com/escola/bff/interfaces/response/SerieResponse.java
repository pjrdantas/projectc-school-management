package br.com.escola.bff.interfaces.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SerieResponse(
        UUID id,
        String nome,
        Integer ordem,
        String nivelEnsino,
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt
) {
}
