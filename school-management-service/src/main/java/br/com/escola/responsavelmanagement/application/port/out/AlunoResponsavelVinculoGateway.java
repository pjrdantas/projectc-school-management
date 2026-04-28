package br.com.escola.responsavelmanagement.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.responsavelmanagement.application.dto.VinculoAlunoResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.VinculoAlunoResponsavelOutput;

public interface AlunoResponsavelVinculoGateway {

    boolean existsByAlunoAndResponsavel(UUID idAluno, UUID idResponsavel);

    VinculoAlunoResponsavelOutput save(VinculoAlunoResponsavelInput input);

    List<VinculoAlunoResponsavelOutput> findByAluno(UUID idAluno);

    void deleteByAlunoAndResponsavel(UUID idAluno, UUID idResponsavel);
}
