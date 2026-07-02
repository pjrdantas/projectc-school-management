package br.com.escola.peopleservice.application.exception;

public class PeopleServiceResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PeopleServiceResourceNotFoundException(String message) {
        super(message);
    }
}
