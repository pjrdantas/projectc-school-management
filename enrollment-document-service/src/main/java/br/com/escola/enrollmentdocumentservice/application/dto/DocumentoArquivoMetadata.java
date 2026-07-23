package br.com.escola.enrollmentdocumentservice.application.dto;

import java.util.UUID;

public record DocumentoArquivoMetadata(
        UUID documentoId,
        String nomeArquivo,
        String tipoConteudo,
        String referenciaArmazenamento) {
}
