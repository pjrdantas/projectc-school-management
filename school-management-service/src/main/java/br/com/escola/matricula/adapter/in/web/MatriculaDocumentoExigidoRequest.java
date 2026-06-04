package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MatriculaDocumentoExigidoRequest(
        @NotNull(message = "tipoMatriculaId é obrigatório")
        UUID tipoMatriculaId,
        @NotNull(message = "tipoDocumentoId é obrigatório")
        UUID tipoDocumentoId,
        Boolean obrigatorio,
        Integer ordem) {
}
