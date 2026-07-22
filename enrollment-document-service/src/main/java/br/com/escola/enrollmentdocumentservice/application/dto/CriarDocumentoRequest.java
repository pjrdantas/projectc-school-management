package br.com.escola.enrollmentdocumentservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarDocumentoRequest(
        @NotBlank(message = "entidadeTipo e obrigatorio")
        String entidadeTipo,
        @NotNull(message = "entidadeId e obrigatorio")
        UUID entidadeId,
        @NotBlank(message = "tipoDocumento e obrigatorio")
        String tipoDocumento,
        String numeroDocumento,
        String caminhoArquivo,
        String observacao) {

    @AssertTrue(message = "caminhoArquivo e obrigatorio")
    public boolean possuiReferenciaArquivo() {
        return caminhoArquivo != null && !caminhoArquivo.isBlank();
    }
}
