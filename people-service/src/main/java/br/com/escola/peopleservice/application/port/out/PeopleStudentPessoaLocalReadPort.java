package br.com.escola.peopleservice.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaAlunoVinculoResponse;

public interface PeopleStudentPessoaLocalReadPort {

    Optional<PessoaAlunoVinculoResponse> buscarVinculoPorAlunoId(UUID alunoId, UUID escolaId);
}
