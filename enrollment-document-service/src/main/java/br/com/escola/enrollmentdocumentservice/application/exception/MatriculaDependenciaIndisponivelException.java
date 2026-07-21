package br.com.escola.enrollmentdocumentservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MatriculaDependenciaIndisponivelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaDependenciaIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
