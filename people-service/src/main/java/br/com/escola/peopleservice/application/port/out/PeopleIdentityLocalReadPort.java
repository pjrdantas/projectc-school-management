package br.com.escola.peopleservice.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;

public interface PeopleIdentityLocalReadPort {

    Optional<PessoaResumoResponse> buscarPessoaPorId(UUID pessoaId, UUID escolaId);
}
