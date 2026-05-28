package br.com.escola.shared.document.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponse(
        UUID id,
        String entidadeTipo,
        UUID entidadeId,
        String tipoDocumento,
        String numeroDocumento,
        String caminhoArquivo,
        LocalDateTime dataUpload,
        String observacao) {
}
