package br.com.escola.responsiblesservice.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AlunoResponsavelVinculoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AlunoResponsavelVinculoNaoEncontradoException() {
        super("Vinculo aluno-responsavel nao encontrado para a escola informada");
    }
}
