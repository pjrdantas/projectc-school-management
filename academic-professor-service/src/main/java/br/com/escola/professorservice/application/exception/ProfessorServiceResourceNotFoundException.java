package br.com.escola.professorservice.application.exception;

public class ProfessorServiceResourceNotFoundException extends RuntimeException {

    public ProfessorServiceResourceNotFoundException(String message) {
        super(message);
    }
}
