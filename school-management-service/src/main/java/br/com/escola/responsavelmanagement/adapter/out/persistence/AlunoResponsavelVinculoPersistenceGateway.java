package br.com.escola.responsavelmanagement.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.responsavelmanagement.adapter.out.persistence.entity.AlunoResponsavelEntity;
import br.com.escola.responsavelmanagement.adapter.out.persistence.repository.AlunoResponsavelJpaRepository;
import br.com.escola.responsavelmanagement.application.dto.VinculoAlunoResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.VinculoAlunoResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.AlunoResponsavelVinculoGateway;

@Component
public class AlunoResponsavelVinculoPersistenceGateway implements AlunoResponsavelVinculoGateway {

    private final AlunoResponsavelJpaRepository alunoResponsavelJpaRepository;

    public AlunoResponsavelVinculoPersistenceGateway(AlunoResponsavelJpaRepository alunoResponsavelJpaRepository) {
        this.alunoResponsavelJpaRepository = alunoResponsavelJpaRepository;
    }

    @Override
    public boolean existsByAlunoAndResponsavel(UUID idAluno, UUID idResponsavel) {
        return alunoResponsavelJpaRepository.existsByIdAlunoAndIdResponsavel(idAluno, idResponsavel);
    }

    @Override
    public VinculoAlunoResponsavelOutput save(VinculoAlunoResponsavelInput input) {
        AlunoResponsavelEntity entity = new AlunoResponsavelEntity();
        entity.setIdAluno(input.idAluno());
        entity.setIdResponsavel(input.idResponsavel());
        return toOutput(alunoResponsavelJpaRepository.save(entity));
    }

    @Override
    public List<VinculoAlunoResponsavelOutput> findByAluno(UUID idAluno) {
        return alunoResponsavelJpaRepository.findByIdAluno(idAluno).stream().map(this::toOutput).toList();
    }

    @Override
    @Transactional
    public void deleteByAlunoAndResponsavel(UUID idAluno, UUID idResponsavel) {
        alunoResponsavelJpaRepository.deleteByIdAlunoAndIdResponsavel(idAluno, idResponsavel);
    }

    private VinculoAlunoResponsavelOutput toOutput(AlunoResponsavelEntity entity) {
        return new VinculoAlunoResponsavelOutput(
                entity.getId(),
                entity.getIdAluno(),
                entity.getIdResponsavel(),
                entity.getCreatedAt());
    }
}
