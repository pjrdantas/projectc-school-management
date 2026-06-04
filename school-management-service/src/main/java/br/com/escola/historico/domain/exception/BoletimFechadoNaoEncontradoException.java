package br.com.escola.historico.domain.exception;

import java.io.Serial;
import java.util.UUID;

public class BoletimFechadoNaoEncontradoException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BoletimFechadoNaoEncontradoException(UUID boletimId) {
        super("Boletim fechado não encontrado: " + boletimId);
    }
}
