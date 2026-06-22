package br.com.escola.catalog.infra.database.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.CommandIdempotencyJpaEntity;

public interface CommandIdempotencyJpaRepository extends JpaRepository<CommandIdempotencyJpaEntity, UUID> {

    Optional<CommandIdempotencyJpaEntity> findByEscolaIdAndKey(UUID escolaId, String key);
}
