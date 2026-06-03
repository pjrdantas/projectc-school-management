package br.com.escola.responsavel.adapter.out.persistence;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.responsavel.adapter.out.persistence.entity.AlunoResponsavelEntity;
import br.com.escola.responsavel.adapter.out.persistence.repository.AlunoResponsavelJpaRepository;
import br.com.escola.responsavel.adapter.out.persistence.repository.ParentescoJpaRepository;
import br.com.escola.responsavel.application.dto.VinculoAlunoResponsavelInput;
import br.com.escola.responsavel.application.dto.VinculoAlunoResponsavelOutput;
import br.com.escola.responsavel.application.port.out.AlunoResponsavelVinculoGateway;

@Component
public class AlunoResponsavelVinculoPersistenceGateway implements AlunoResponsavelVinculoGateway {

    private final AlunoResponsavelJpaRepository alunoResponsavelJpaRepository;
    private final ParentescoJpaRepository parentescoJpaRepository;

    public AlunoResponsavelVinculoPersistenceGateway(
            AlunoResponsavelJpaRepository alunoResponsavelJpaRepository,
            ParentescoJpaRepository parentescoJpaRepository) {
        this.alunoResponsavelJpaRepository = alunoResponsavelJpaRepository;
        this.parentescoJpaRepository = parentescoJpaRepository;
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
        entity.setParentesco(parentesco(input.parentesco()));
        entity.setResponsavelFinanceiro(Boolean.TRUE.equals(input.responsavelFinanceiro()));
        entity.setResponsavelPedagogico(Boolean.TRUE.equals(input.responsavelPedagogico()));
        entity.setAutorizadoRetirar(Boolean.TRUE.equals(input.autorizadoRetirar()));
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
                entity.getParentesco(),
                entity.getResponsavelFinanceiro(),
                entity.getResponsavelPedagogico(),
                entity.getAutorizadoRetirar(),
                entity.getCreatedAt());
    }

    private br.com.escola.responsavel.adapter.out.persistence.entity.ParentescoEntity parentesco(String codigo) {
        String codigoNormalizado = codigo == null || codigo.isBlank()
                ? "RESPONSAVEL_LEGAL"
                : codigo.trim().toUpperCase(Locale.ROOT);
        return parentescoJpaRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Parentesco nao cadastrado: " + codigoNormalizado));
    }
}
