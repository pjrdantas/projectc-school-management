package br.com.escola.catalog.infra.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.catalog.infra.database.entity.NivelEnsinoJpaEntity;

public interface NivelEnsinoJpaRepository extends JpaRepository<NivelEnsinoJpaEntity, UUID> {

    List<NivelEnsinoJpaEntity> findAllByOrderByCodigoAsc();
}
