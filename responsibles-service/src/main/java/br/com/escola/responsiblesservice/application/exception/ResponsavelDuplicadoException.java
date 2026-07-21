package br.com.escola.responsiblesservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResponsavelDuplicadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResponsavelDuplicadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
