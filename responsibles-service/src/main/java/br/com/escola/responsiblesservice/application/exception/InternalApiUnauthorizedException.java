package br.com.escola.responsiblesservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InternalApiUnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public InternalApiUnauthorizedException(String message) {
        super(message);
    }
}
