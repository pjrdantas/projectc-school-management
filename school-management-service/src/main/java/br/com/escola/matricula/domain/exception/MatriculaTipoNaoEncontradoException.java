package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaTipoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaTipoNaoEncontradoException(UUID id) {
        super("Tipo de matrícula não encontrado para o id " + id);
    }
}
