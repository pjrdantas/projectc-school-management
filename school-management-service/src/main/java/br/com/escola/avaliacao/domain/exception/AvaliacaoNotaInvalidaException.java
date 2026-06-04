package br.com.escola.avaliacao.domain.exception;

import java.math.BigDecimal;

public class AvaliacaoNotaInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoNotaInvalidaException(BigDecimal nota, BigDecimal valorMaximo) {
        super("Nota " + nota + " não pode ser maior que o valor máximo " + valorMaximo);
    }
}
