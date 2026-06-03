package br.com.escola.catalogo.adapter.in.web.dto;

import java.util.UUID;

public record TurnoResponse(
        UUID id,
        String codigo,
        String descricao) {
}
