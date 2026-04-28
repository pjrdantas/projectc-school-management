package br.com.escola.responsavelmanagement.domain.exception;

import java.util.UUID;

public class ResponsavelNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResponsavelNaoEncontradoException(UUID id) {
        super("Responsável não encontrado para o id " + id);
    }
}
