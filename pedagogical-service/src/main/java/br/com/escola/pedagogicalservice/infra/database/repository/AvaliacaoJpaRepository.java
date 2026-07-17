package br.com.escola.pedagogicalservice.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.pedagogicalservice.infra.database.entity.AvaliacaoJpaEntity;

public interface AvaliacaoJpaRepository extends JpaRepository<AvaliacaoJpaEntity, UUID> {

    Optional<AvaliacaoJpaEntity> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<AvaliacaoJpaEntity> findAllBySchoolIdOrderByCreatedAtDescIdAsc(UUID schoolId);
}
