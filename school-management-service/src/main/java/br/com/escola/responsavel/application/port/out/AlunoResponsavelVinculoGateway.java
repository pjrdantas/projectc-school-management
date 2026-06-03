package br.com.escola.responsavel.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.responsavel.application.dto.VinculoAlunoResponsavelInput;
import br.com.escola.responsavel.application.dto.VinculoAlunoResponsavelOutput;

public interface AlunoResponsavelVinculoGateway {

    boolean existsByAlunoAndResponsavel(UUID idAluno, UUID idResponsavel);

    VinculoAlunoResponsavelOutput save(VinculoAlunoResponsavelInput input);

    List<VinculoAlunoResponsavelOutput> findByAluno(UUID idAluno);

    void deleteByAlunoAndResponsavel(UUID idAluno, UUID idResponsavel);
}
