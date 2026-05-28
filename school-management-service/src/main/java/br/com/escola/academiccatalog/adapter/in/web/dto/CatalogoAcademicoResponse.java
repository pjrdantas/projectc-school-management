package br.com.escola.academiccatalog.adapter.in.web.dto;

import java.util.UUID;

public record CatalogoAcademicoResponse(
        UUID id,
        String codigo,
        String descricao) {
}

