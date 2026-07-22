package br.com.escola.enrollmentdocumentservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarDocumentoAlunoRequest(
        @NotNull(message = "alunoId e obrigatorio")
        UUID alunoId,
        @NotBlank(message = "tipoDocumento e obrigatorio")
        String tipoDocumento,
        String nomeArquivo,
        String urlArquivo,
        String numeroDocumento,
        String caminhoArquivo,
        String observacao) {

    @AssertTrue(message = "caminhoArquivo ou urlArquivo e obrigatorio")
    public boolean possuiReferenciaArquivo() {
        return possuiTexto(caminhoArquivo) || possuiTexto(urlArquivo);
    }

    private boolean possuiTexto(String value) {
        return value != null && !value.isBlank();
    }
}
