package br.com.escola.catalogo.application.dto;

import java.util.UUID;

public record SerieInput(
        String nome,
        Integer ordem,
        String nivelEnsino,
        UUID escolaId) {
}
