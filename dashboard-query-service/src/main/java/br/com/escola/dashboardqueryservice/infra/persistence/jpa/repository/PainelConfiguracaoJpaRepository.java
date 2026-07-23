package br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelConfiguracaoJpaEntity;

public interface PainelConfiguracaoJpaRepository extends JpaRepository<PainelConfiguracaoJpaEntity, UUID> {

    Optional<PainelConfiguracaoJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    Optional<PainelConfiguracaoJpaEntity> findByEscolaIdAndCodigoIgnoreCase(UUID escolaId, String codigo);

    List<PainelConfiguracaoJpaEntity> findByEscolaIdAndPublicoIdOrderByCodigoAsc(UUID escolaId, UUID publicoId);

    List<PainelConfiguracaoJpaEntity> findByEscolaIdOrderByCodigoAsc(UUID escolaId);

    boolean existsByEscolaIdAndPublicoId(UUID escolaId, UUID publicoId);
}
