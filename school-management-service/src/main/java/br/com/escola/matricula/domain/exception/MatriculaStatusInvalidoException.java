package br.com.escola.matricula.domain.exception;

public class MatriculaStatusInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaStatusInvalidoException(String status) {
        super("Status de matrícula inválido: " + status);
    }
}
