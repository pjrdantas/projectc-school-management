package br.com.escola.responsiblesservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResponsavelComAlunoVinculadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResponsavelComAlunoVinculadoException() {
        super("Responsavel nao pode ser excluido enquanto possuir aluno vinculado");
    }
}
