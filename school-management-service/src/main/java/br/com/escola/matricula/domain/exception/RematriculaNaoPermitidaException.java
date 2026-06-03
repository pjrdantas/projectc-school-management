package br.com.escola.matricula.domain.exception;

public class RematriculaNaoPermitidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public RematriculaNaoPermitidaException(String motivo) {
        super("Rematrícula não permitida: " + motivo);
    }
}
