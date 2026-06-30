package br.com.escola.professor.application.port.internal;

import java.util.UUID;

public interface ProfessorPessoaPort {

    boolean existeProfessorPorPessoa(UUID escolaId, UUID pessoaId);
}
