package br.com.escola.responsiblesservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AlunoDependenciaIndisponivelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AlunoDependenciaIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
