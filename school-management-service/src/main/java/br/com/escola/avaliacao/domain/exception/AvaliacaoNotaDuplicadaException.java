package br.com.escola.avaliacao.domain.exception;

public class AvaliacaoNotaDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoNotaDuplicadaException() {
        super("Nota já lançada para este aluno nesta avaliação.");
    }
}
