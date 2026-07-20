package br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboardqueryservice.application.dto.TipoPainelProjecao;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelProjecaoJpaEntity;

public interface PainelProjecaoJpaRepository extends JpaRepository<PainelProjecaoJpaEntity, UUID> {

    Optional<PainelProjecaoJpaEntity> findByEscolaIdAndChaveProjecao(UUID escolaId, String chaveProjecao);

    List<PainelProjecaoJpaEntity> findByEscolaIdAndTipoOrderByUpdatedAtDesc(
            UUID escolaId,
            TipoPainelProjecao tipo);
}
