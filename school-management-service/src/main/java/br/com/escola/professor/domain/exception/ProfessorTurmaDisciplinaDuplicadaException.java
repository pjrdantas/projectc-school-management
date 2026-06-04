package br.com.escola.professor.domain.exception;

import java.util.UUID;

public class ProfessorTurmaDisciplinaDuplicadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProfessorTurmaDisciplinaDuplicadaException(UUID professorId, UUID turmaDisciplinaId) {
        super("Professor " + professorId + " já está vinculado à turma/disciplina " + turmaDisciplinaId);
    }
}
