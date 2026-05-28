package br.com.escola.responsavelmanagement.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.responsavelmanagement.adapter.out.persistence.entity.AlunoResponsavelEntity;

public interface AlunoResponsavelJpaRepository extends JpaRepository<AlunoResponsavelEntity, UUID> {

    boolean existsByIdAlunoAndIdResponsavel(UUID idAluno, UUID idResponsavel);

    List<AlunoResponsavelEntity> findByIdAluno(UUID idAluno);

    long countByIdResponsavel(UUID idResponsavel);

    void deleteByIdAluno(UUID idAluno);

    void deleteByIdAlunoAndIdResponsavel(UUID idAluno, UUID idResponsavel);
}
