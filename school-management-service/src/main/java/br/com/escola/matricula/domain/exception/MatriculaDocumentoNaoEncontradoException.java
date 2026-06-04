package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaDocumentoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaDocumentoNaoEncontradoException(UUID id) {
        super("Documento não encontrado para vínculo com matrícula: " + id);
    }
}
