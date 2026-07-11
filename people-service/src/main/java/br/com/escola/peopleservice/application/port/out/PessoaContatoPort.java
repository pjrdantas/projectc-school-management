package br.com.escola.peopleservice.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaContatoResponse;

public interface PessoaContatoPort {

    Optional<PessoaContatoResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId);
}
