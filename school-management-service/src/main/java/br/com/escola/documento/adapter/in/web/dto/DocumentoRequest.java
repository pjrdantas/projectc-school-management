package br.com.escola.documento.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoRequest(
        @NotBlank(message = "entidadeTipo é obrigatório")
        String entidadeTipo,
        @NotNull(message = "entidadeId é obrigatório")
        UUID entidadeId,
        @NotBlank(message = "tipoDocumento é obrigatório")
        String tipoDocumento,
        @NotBlank(message = "numeroDocumento é obrigatório")
        String numeroDocumento,
        @NotBlank(message = "caminhoArquivo é obrigatório")
        String caminhoArquivo,
        String observacao) {
}
