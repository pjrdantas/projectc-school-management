package br.com.escola.catalog.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.DisciplinaJpaEntity;

public interface DisciplinaJpaRepository extends JpaRepository<DisciplinaJpaEntity, UUID> {

    Optional<DisciplinaJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    List<DisciplinaJpaEntity> findAllByEscolaIdOrderByNomeAsc(UUID escolaId);
}

