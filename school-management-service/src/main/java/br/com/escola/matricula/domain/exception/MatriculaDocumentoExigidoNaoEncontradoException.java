package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaDocumentoExigidoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaDocumentoExigidoNaoEncontradoException(UUID id) {
        super("Documento exigido da matrícula não encontrado para o id " + id);
    }
}
