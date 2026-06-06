package br.com.escola.avaliacao.domain.exception;

public class AvaliacaoMatriculaTurmaInconsistenteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AvaliacaoMatriculaTurmaInconsistenteException() {
        super("A matrícula não pertence à turma desta avaliação.");
    }
}
