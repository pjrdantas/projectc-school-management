package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class ProfessorTurmaDisciplinaNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorTurmaDisciplinaNaoEncontradaException(UUID id) {
        super("Turma/disciplina não encontrada para o id " + id);
    }
}
