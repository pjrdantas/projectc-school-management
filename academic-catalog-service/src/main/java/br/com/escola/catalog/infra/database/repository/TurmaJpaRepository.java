package br.com.escola.catalog.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.TurmaJpaEntity;

public interface TurmaJpaRepository extends JpaRepository<TurmaJpaEntity, UUID> {

    Optional<TurmaJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    List<TurmaJpaEntity> findAllByEscolaIdOrderByCodigoAsc(UUID escolaId);
}

