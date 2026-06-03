package br.com.escola.documento.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoAlunoResponse(
        UUID id,
        UUID alunoId,
        String tipoDocumento,
        String nomeArquivo,
        String urlArquivo,
        String numeroDocumento,
        String caminhoArquivo,
        LocalDateTime dataUpload,
        String observacao) {
}
