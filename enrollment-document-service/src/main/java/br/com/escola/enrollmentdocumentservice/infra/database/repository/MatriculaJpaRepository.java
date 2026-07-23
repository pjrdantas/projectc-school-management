package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.MatriculaJpaEntity;

public interface MatriculaJpaRepository extends JpaRepository<MatriculaJpaEntity, UUID> {
    List<MatriculaJpaEntity> findAllBySchoolIdOrderByCreatedAtDescIdAsc(UUID schoolId);

    Optional<MatriculaJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);

    boolean existsBySchoolIdAndAlunoIdAndTurmaIdAndPeriodoLetivoIdAndStatus(
            UUID schoolId,
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status);

    boolean existsBySchoolIdAndAlunoIdAndTurmaIdAndPeriodoLetivoIdAndStatusAndIdNot(
            UUID schoolId,
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status,
            UUID id);
}
