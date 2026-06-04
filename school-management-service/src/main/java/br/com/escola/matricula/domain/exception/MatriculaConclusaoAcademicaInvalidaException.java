package br.com.escola.matricula.domain.exception;

import java.io.Serial;

public class MatriculaConclusaoAcademicaInvalidaException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MatriculaConclusaoAcademicaInvalidaException(String message) {
        super(message);
    }
}
