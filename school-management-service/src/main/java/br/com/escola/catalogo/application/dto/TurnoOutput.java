package br.com.escola.catalogo.application.dto;

import java.util.UUID;

public record TurnoOutput(
        UUID id,
        String codigo,
        String descricao) {
}
