package br.com.escola.catalog.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.SerieJpaEntity;

public interface SerieJpaRepository extends JpaRepository<SerieJpaEntity, UUID> {

    Optional<SerieJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    List<SerieJpaEntity> findAllByEscolaIdOrderByOrdemAscNomeAsc(UUID escolaId);
}

