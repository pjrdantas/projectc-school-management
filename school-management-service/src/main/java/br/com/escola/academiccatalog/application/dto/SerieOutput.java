package br.com.escola.academiccatalog.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SerieOutput(
        UUID id,
        String nome,
        Integer ordem,
        String nivelEnsino,
        LocalDateTime createdAt) {
}
