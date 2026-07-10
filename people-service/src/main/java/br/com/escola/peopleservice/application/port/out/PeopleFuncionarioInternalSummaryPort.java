package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaFuncionarioInternalSummaryResponse;

public interface PeopleFuncionarioInternalSummaryPort {

    Optional<PessoaFuncionarioInternalSummaryResponse> buscarFuncionarioPorId(UUID funcionarioId, UUID escolaId);

    List<PessoaFuncionarioInternalSummaryResponse> listarFuncionariosAtivosPorEscola(UUID escolaId);
}
