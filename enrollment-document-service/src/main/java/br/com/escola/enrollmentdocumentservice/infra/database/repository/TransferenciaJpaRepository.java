package br.com.escola.enrollmentdocumentservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.TransferenciaJpaEntity;

public interface TransferenciaJpaRepository extends JpaRepository<TransferenciaJpaEntity, UUID> {
    Optional<TransferenciaJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);
    List<TransferenciaJpaEntity> findAllBySchoolIdAndAlunoIdOrderByCreatedAtDescIdAsc(UUID schoolId, UUID alunoId);
}
