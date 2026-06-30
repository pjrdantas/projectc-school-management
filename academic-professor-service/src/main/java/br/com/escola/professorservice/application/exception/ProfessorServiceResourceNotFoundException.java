package br.com.escola.professorservice.application.exception;

public class ProfessorServiceResourceNotFoundException extends RuntimeException {



	private static final long serialVersionUID = 1L;

	public ProfessorServiceResourceNotFoundException(String message) {
        super(message);
    }
}
