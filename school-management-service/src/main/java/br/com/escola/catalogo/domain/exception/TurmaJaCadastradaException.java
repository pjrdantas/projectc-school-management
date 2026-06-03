package br.com.escola.catalogo.domain.exception;

import java.util.UUID;

public class TurmaJaCadastradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TurmaJaCadastradaException(String codigo, UUID periodoLetivoId) {
        super("Já existe turma com código " + codigo + " para o período letivo " + periodoLetivoId);
    }
}
