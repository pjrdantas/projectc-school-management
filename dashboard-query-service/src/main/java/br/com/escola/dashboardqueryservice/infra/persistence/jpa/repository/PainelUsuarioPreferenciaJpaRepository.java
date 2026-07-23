package br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelUsuarioPreferenciaJpaEntity;

public interface PainelUsuarioPreferenciaJpaRepository extends JpaRepository<PainelUsuarioPreferenciaJpaEntity, UUID> {

    List<PainelUsuarioPreferenciaJpaEntity> findByEscolaIdAndUsuarioIdOrderByUpdatedAtDesc(UUID escolaId, UUID usuarioId);

    Optional<PainelUsuarioPreferenciaJpaEntity> findByEscolaIdAndUsuarioIdAndWidgetId(
            UUID escolaId, UUID usuarioId, UUID widgetId);

    boolean existsByEscolaIdAndWidgetId(UUID escolaId, UUID widgetId);
}
