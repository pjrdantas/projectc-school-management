package br.com.escola.matricula.domain.exception;

public class TransferenciaDadosObrigatoriosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public TransferenciaDadosObrigatoriosException() {
        super("Dados de transferência são obrigatórios para matrícula por TRANSFERENCIA");
    }
}
