package br.com.escola.responsavelmanagement.domain.exception;

import java.util.UUID;

public class AlunoResponsavelVinculoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AlunoResponsavelVinculoNaoEncontradoException(UUID idAluno, UUID idResponsavel) {
        super("Vínculo não encontrado para aluno " + idAluno + " e responsável " + idResponsavel);
    }
}
