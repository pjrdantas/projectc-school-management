package br.com.escola.documento.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoAlunoRequest(
        @NotNull(message = "alunoId é obrigatório")
        UUID alunoId,
        @NotBlank(message = "tipoDocumento é obrigatório")
        String tipoDocumento,
        String nomeArquivo,
        String urlArquivo,
        String numeroDocumento,
        String caminhoArquivo,
        String observacao) {
}
