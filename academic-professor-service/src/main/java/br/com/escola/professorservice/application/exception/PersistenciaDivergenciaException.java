package br.com.escola.professorservice.application.exception;

public class PersistenciaDivergenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public PersistenciaDivergenciaException(String message) {
        super(message);
    }
}

