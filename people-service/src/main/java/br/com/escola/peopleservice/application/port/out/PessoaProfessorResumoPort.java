package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.peopleservice.application.dto.PessoaProfessorResumoResponse;

public interface PessoaProfessorResumoPort {

    Optional<PessoaProfessorResumoResponse> buscarProfessorPorId(UUID professorId, UUID escolaId);

    List<PessoaProfessorResumoResponse> listarProfessoresPorEscola(UUID escolaId);
}
