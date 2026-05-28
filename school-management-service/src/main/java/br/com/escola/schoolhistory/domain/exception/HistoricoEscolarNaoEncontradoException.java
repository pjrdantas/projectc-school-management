package br.com.escola.schoolhistory.domain.exception;

import java.util.UUID;

public class HistoricoEscolarNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public HistoricoEscolarNaoEncontradoException(UUID id) {
        super("Histórico escolar não encontrado: " + id);
    }
}
