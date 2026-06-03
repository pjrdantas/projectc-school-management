package br.com.escola.catalogo.domain.exception;

import java.util.UUID;

public class SerieNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SerieNaoEncontradaException(UUID id) {
        super("Série não encontrada para o id " + id);
    }
}
