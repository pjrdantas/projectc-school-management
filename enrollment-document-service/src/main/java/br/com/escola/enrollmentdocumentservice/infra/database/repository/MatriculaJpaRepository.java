package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.MatriculaJpaEntity;

public interface MatriculaJpaRepository extends JpaRepository<MatriculaJpaEntity, UUID> {
    List<MatriculaJpaEntity> findAllBySchoolIdOrderByCreatedAtDescIdAsc(UUID schoolId);
}
