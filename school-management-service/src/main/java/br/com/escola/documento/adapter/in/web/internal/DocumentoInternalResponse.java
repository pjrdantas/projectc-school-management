package br.com.escola.documento.adapter.in.web.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoInternalResponse(
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
