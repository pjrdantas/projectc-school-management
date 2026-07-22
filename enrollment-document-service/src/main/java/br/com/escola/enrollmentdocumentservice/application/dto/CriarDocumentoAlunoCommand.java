package br.com.escola.enrollmentdocumentservice.application.dto;

import java.util.UUID;

public record CriarDocumentoAlunoCommand(
        UUID alunoId,
        String tipoDocumento,
        String nomeArquivo,
        String urlArquivo,
        String numeroDocumento,
        String caminhoArquivo,
        String observacao,
        String tipoConteudo,
        Long tamanhoArquivo) {
}
