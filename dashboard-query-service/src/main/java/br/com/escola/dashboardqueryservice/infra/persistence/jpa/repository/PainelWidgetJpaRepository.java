package br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelWidgetJpaEntity;

public interface PainelWidgetJpaRepository extends JpaRepository<PainelWidgetJpaEntity, UUID> {

    boolean existsByEscolaIdAndPainelId(UUID escolaId, UUID painelId);

    Optional<PainelWidgetJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    Optional<PainelWidgetJpaEntity> findByEscolaIdAndPainelIdAndCodigoIgnoreCase(UUID escolaId, UUID painelId, String codigo);

    Optional<PainelWidgetJpaEntity> findByEscolaIdAndPainelIdAndOrdem(UUID escolaId, UUID painelId, int ordem);

    List<PainelWidgetJpaEntity> findByEscolaIdAndPainelIdOrderByOrdemAsc(UUID escolaId, UUID painelId);
}
