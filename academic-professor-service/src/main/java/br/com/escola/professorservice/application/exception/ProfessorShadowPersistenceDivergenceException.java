package br.com.escola.professorservice.application.exception;

public class ProfessorShadowPersistenceDivergenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public ProfessorShadowPersistenceDivergenceException(String message) {
        super(message);
    }
}
