package br.com.escola.documento.domain.exception;

public class DocumentoInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DocumentoInvalidoException(String message) {
        super(message);
    }
}
