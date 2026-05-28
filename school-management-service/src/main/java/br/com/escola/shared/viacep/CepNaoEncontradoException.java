package br.com.escola.shared.viacep;

public class CepNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public CepNaoEncontradoException(String cep) {
        super("CEP não encontrado: " + cep);
    }
}
