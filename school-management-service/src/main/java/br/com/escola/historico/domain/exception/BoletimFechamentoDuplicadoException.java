package br.com.escola.historico.domain.exception;

import java.io.Serial;
import java.util.UUID;

public class BoletimFechamentoDuplicadoException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BoletimFechamentoDuplicadoException(UUID matriculaId, String periodoReferencia) {
        super("Já existe boletim fechado para a matrícula %s no período %s"
                .formatted(matriculaId, periodoReferencia));
    }
}
