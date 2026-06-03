package br.com.escola.responsavel.domain.exception;

public class AlunoResponsavelVinculoDuplicadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AlunoResponsavelVinculoDuplicadoException() {
        super("Vínculo entre aluno e responsável já existe");
    }
}
