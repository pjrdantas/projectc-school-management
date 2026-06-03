package br.com.escola.documento.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoOutput(
        UUID id,
        String entidadeTipo,
        UUID entidadeId,
        String tipoDocumento,
        String numeroDocumento,
        String caminhoArquivo,
        LocalDateTime dataUpload,
        String observacao) {
}
