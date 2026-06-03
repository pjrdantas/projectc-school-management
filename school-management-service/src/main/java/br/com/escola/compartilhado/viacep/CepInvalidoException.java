package br.com.escola.compartilhado.viacep;

public class CepInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public CepInvalidoException() {
        super("CEP deve possuir 8 dígitos");
    }
}
