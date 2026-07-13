package br.com.escola.enrollmentdocumentservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponse(
        UUID id,
        String entidadeTipo,
        UUID entidadeId,
        UUID escolaId,
        String escolaNome,
        String tipoDocumento,
        String numeroDocumento,
        String caminhoArquivo,
        LocalDateTime dataUpload,
        String observacao) {
}
