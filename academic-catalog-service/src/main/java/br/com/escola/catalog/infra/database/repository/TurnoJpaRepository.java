package br.com.escola.catalog.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.TurnoJpaEntity;

public interface TurnoJpaRepository extends JpaRepository<TurnoJpaEntity, UUID> {

    Optional<TurnoJpaEntity> findByCodigoIgnoreCase(String codigo);

    List<TurnoJpaEntity> findAllByOrderByCodigoAsc();
}

