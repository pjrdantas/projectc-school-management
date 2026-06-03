package br.com.escola.catalogo.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SerieResponse(
        UUID id,
        String nome,
        Integer ordem,
        String nivelEnsino,
        LocalDateTime createdAt) {
}
