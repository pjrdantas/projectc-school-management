package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaFuncionarioResumoResponse;

public interface PessoaFuncionarioResumoPort {

    Optional<PessoaFuncionarioResumoResponse> buscarFuncionarioPorId(UUID funcionarioId, UUID escolaId);

    List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(UUID escolaId);
}
