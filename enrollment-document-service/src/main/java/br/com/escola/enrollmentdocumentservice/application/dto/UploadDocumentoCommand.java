package br.com.escola.enrollmentdocumentservice.application.dto;

import java.io.InputStream;
import java.util.UUID;

public record UploadDocumentoCommand(
        String entidadeTipo,
        UUID entidadeId,
        String tipoDocumento,
        String numeroDocumento,
        String observacao,
        String nomeArquivo,
        String tipoConteudo,
        long tamanhoArquivo,
        InputStream conteudo) {
}
