package br.com.escola.documento.domain.exception;

import java.util.UUID;

public class DocumentoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DocumentoNaoEncontradoException(UUID id) {
        super("Documento não encontrado: " + id);
    }
}
