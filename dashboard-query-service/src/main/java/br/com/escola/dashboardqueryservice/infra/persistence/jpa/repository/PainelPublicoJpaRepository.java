package br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelPublicoJpaEntity;

public interface PainelPublicoJpaRepository extends JpaRepository<PainelPublicoJpaEntity, UUID> {

    Optional<PainelPublicoJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    Optional<PainelPublicoJpaEntity> findByEscolaIdAndCodigoIgnoreCase(UUID escolaId, String codigo);

    List<PainelPublicoJpaEntity> findByEscolaIdOrderByCodigoAsc(UUID escolaId);
}
