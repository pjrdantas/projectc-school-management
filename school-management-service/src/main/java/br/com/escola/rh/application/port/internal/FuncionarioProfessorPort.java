package br.com.escola.rh.application.port.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.rh.application.dto.internal.FuncionarioProfessorResumo;

public interface FuncionarioProfessorPort {

    Optional<FuncionarioProfessorResumo> buscarFuncionarioParaProfessor(UUID escolaId, UUID funcionarioId);

    List<FuncionarioProfessorResumo> listarFuncionariosElegiveisParaProfessor(UUID escolaId);
}
