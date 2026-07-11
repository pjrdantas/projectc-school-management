package br.com.escola.peopleservice.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PessoaDocumentoMetadataResponse(
        UUID pessoaDocumentoId,
        UUID pessoaId,
        UUID documentoId,
        UUID tipoDocumentoId,
        String tipoDocumentoCodigo,
        String tipoDocumentoDescricao,
        String numeroDocumento,
        String caminhoArquivo,
        String observacao,
        OffsetDateTime dataUpload) {
}


