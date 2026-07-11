package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaProfessorInternalSummaryResponse;

public interface PeopleProfessorInternalSummaryPort {

    Optional<PessoaProfessorInternalSummaryResponse> buscarProfessorPorId(UUID professorId, UUID escolaId);

    List<PessoaProfessorInternalSummaryResponse> listarProfessoresPorEscola(UUID escolaId);
}
