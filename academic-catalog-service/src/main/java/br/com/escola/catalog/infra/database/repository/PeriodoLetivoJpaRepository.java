package br.com.escola.catalog.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.PeriodoLetivoJpaEntity;

public interface PeriodoLetivoJpaRepository extends JpaRepository<PeriodoLetivoJpaEntity, UUID> {

    Optional<PeriodoLetivoJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    List<PeriodoLetivoJpaEntity> findAllByEscolaIdOrderByAnoDescNomeAsc(UUID escolaId);
}

