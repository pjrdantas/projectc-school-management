package br.com.escola.pedagogicalservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.AulaJpaEntity;

public interface AulaJpaRepository extends JpaRepository<AulaJpaEntity, UUID> {

    Optional<AulaJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<AulaJpaEntity> findAllBySchoolIdOrderByCreatedAtDescIdAsc(UUID schoolId);
}
