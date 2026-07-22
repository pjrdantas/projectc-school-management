package br.com.escola.enrollmentdocumentservice.application.dto;

public record DocumentoArquivoExclusao(
        String referenciaArmazenamento,
        boolean excluidoAgora) {
}
