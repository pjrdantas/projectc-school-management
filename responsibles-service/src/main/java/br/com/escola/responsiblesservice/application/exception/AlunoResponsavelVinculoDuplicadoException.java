package br.com.escola.responsiblesservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlunoResponsavelVinculoDuplicadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AlunoResponsavelVinculoDuplicadoException() {
        super("Aluno ja possui vinculo com este responsavel");
    }
}
