package br.com.escola.peopleservice.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaContatoLocalReadResponse;

public interface PeopleContactLocalReadPort {

    Optional<PessoaContatoLocalReadResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId);
}
