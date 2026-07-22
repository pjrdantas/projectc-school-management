package br.com.escola.enrollmentdocumentservice.application.dto;

import java.io.InputStream;

public record DocumentoArquivoDownload(
        String nomeArquivo,
        String tipoConteudo,
        InputStream conteudo) {
}
