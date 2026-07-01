package br.com.escola.professor.domain.exception;

public class DiarioClasseLancamentoDuplicadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DiarioClasseLancamentoDuplicadoException() {
        super("Diario de classe ja possui lancamento para a data informada.");
    }
}
