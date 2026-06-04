package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaTipoDocumentoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaTipoDocumentoNaoEncontradoException(UUID id) {
        super("Tipo de documento não encontrado para o id " + id);
    }
}
