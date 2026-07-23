package br.com.escola.responsiblesservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DadosResponsavelInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DadosResponsavelInvalidosException(String message) {
        super(message);
    }
}
