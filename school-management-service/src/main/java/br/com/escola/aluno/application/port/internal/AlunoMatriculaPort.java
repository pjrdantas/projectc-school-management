package br.com.escola.aluno.application.port.internal;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;

public interface AlunoMatriculaPort {

    boolean existeAlunoPorIdEEscola(UUID alunoId, UUID escolaId);

    Optional<AlunoEntity> buscarAlunoPorIdEEscola(UUID alunoId, UUID escolaId);
}
