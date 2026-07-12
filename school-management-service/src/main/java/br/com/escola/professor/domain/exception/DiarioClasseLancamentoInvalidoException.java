package br.com.escola.professor.domain.exception;

public class DiarioClasseLancamentoInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DiarioClasseLancamentoInvalidoException(String message) {
        super(message);
    }
}
