package br.com.escola.compartilhado.viacep;

public class ViaCepIndisponivelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public ViaCepIndisponivelException() {
        super("Serviço ViaCEP indisponível no momento. Tente novamente mais tarde.");
    }
}
