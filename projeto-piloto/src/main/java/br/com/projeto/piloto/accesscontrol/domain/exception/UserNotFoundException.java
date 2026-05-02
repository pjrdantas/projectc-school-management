package br.com.projeto.piloto.accesscontrol.domain.exception;

public class UserNotFoundException extends DomainException {


	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String message) {
        super(message);
    }
}
