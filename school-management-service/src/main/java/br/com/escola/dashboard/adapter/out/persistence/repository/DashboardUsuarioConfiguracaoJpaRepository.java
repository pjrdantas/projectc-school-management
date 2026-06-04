package br.com.escola.dashboard.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardUsuarioConfiguracaoEntity;

public interface DashboardUsuarioConfiguracaoJpaRepository
        extends JpaRepository<DashboardUsuarioConfiguracaoEntity, UUID> {

    Optional<DashboardUsuarioConfiguracaoEntity> findByUsuarioIdAndDashboardWidgetId(
            UUID usuarioId,
            UUID dashboardWidgetId);

    List<DashboardUsuarioConfiguracaoEntity> findByUsuarioId(UUID usuarioId);

    List<DashboardUsuarioConfiguracaoEntity> findByUsuarioIdOrderByOrdemAscDashboardWidgetTituloAsc(UUID usuarioId);
}
