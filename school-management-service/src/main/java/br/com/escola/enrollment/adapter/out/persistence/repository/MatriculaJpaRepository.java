package br.com.escola.enrollment.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.escola.enrollment.adapter.out.persistence.entity.MatriculaEntity;
public interface MatriculaJpaRepository extends JpaRepository<MatriculaEntity, UUID>, JpaSpecificationExecutor<MatriculaEntity> {

    long countByTurma_IdAndStatus_CodigoNotIn(UUID turmaId, Iterable<String> statusCodigos);

    boolean existsByAluno_IdAndPeriodoLetivo_Id(UUID alunoId, UUID periodoLetivoId);

    boolean existsByAluno_IdAndPeriodoLetivo_IdAndIdNot(
            UUID alunoId,
            UUID periodoLetivoId,
            UUID id);

    Optional<MatriculaEntity> findFirstByAluno_IdAndPeriodoLetivo_IdNotOrderByDataSolicitacaoDescCreatedAtDesc(
            UUID alunoId,
            UUID periodoLetivoId);
}
