package br.com.escola.enrollment.adapter.in.web;

import java.util.UUID;

public record CatalogoMatriculaResponse(
        UUID id,
        String codigo,
        String descricao) {
}

