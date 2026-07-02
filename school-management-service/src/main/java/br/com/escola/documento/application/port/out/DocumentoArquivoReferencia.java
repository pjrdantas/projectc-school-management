package br.com.escola.documento.application.port.out;

import java.util.Objects;

import br.com.escola.documento.domain.exception.DocumentoInvalidoException;

public record DocumentoArquivoReferencia(
        DocumentoArquivoStorageTipo tipo,
        String chave,
        String localizacao) {

    public DocumentoArquivoReferencia {
        Objects.requireNonNull(tipo, "tipo");
        chave = obrigatorio(chave, "chave");
        localizacao = obrigatorio(localizacao, "localizacao");
    }

    public static DocumentoArquivoReferencia local(String chave, String localizacao) {
        return new DocumentoArquivoReferencia(DocumentoArquivoStorageTipo.LOCAL, chave, localizacao);
    }

    public static DocumentoArquivoReferencia objectStorage(String chave, String localizacao) {
        return new DocumentoArquivoReferencia(DocumentoArquivoStorageTipo.OBJECT_STORAGE, chave, localizacao);
    }

    public String caminhoPersistencia() {
        return localizacao;
    }

    private static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DocumentoInvalidoException("Referência de arquivo inválida: " + campo);
        }
        return valor.trim();
    }
}
