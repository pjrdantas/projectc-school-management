package br.com.escola.enrollment.domain.exception;

public class MatriculaTipoInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public MatriculaTipoInvalidoException(String tipo) {
        super("Tipo de matrícula inválido: " + tipo);
    }
}
