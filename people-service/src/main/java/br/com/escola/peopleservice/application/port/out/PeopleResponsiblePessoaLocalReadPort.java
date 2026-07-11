package br.com.escola.peopleservice.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculoResponse;

public interface PeopleResponsiblePessoaLocalReadPort {

    Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId);
}
