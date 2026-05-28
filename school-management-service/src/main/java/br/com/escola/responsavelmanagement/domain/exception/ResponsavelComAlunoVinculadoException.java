package br.com.escola.responsavelmanagement.domain.exception;

import java.util.UUID;

public class ResponsavelComAlunoVinculadoException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

	public ResponsavelComAlunoVinculadoException(UUID id) {
        super("Responsável não pode ser excluído porque ainda está vinculado a outro aluno: " + id);
    }
}
