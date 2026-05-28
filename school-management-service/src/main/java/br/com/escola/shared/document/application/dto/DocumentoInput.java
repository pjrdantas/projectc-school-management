package br.com.escola.shared.document.application.dto;

import java.util.UUID;

public record DocumentoInput(
        String entidadeTipo,
        UUID entidadeId,
        String tipoDocumento,
        String numeroDocumento,
        String caminhoArquivo,
        String observacao) {
}
