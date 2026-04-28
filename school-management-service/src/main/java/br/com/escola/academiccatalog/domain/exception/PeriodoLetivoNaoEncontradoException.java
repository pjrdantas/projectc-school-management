package br.com.escola.academiccatalog.domain.exception;

import java.util.UUID;

public class PeriodoLetivoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PeriodoLetivoNaoEncontradoException(UUID id) {
        super("Período letivo não encontrado para o id " + id);
    }
}
