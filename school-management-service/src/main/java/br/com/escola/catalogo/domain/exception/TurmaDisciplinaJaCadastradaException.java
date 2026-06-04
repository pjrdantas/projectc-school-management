package br.com.escola.catalogo.domain.exception;

import java.util.UUID;

public class TurmaDisciplinaJaCadastradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TurmaDisciplinaJaCadastradaException(UUID turmaId, UUID disciplinaId) {
        super("A disciplina " + disciplinaId + " já está vinculada à turma " + turmaId);
    }
}
