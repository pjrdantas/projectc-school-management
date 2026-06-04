package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MatriculaDocumentoEntregueRequest(
        @NotNull(message = "documentoId é obrigatório")
        UUID documentoId,
        Boolean conferido,
        UUID conferidoPor,
        String observacao) {
}
