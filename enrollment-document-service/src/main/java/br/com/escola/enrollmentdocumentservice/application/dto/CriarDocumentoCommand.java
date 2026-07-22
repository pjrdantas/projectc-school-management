package br.com.escola.enrollmentdocumentservice.application.dto;

import java.util.UUID;

public record CriarDocumentoCommand(
        String entidadeTipo,
        UUID entidadeId,
        String tipoDocumento,
        String nomeArquivo,
        String numeroDocumento,
        String caminhoArquivo,
        String observacao,
        String tipoConteudo,
        Long tamanhoArquivo) {
}
