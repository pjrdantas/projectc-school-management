package br.com.escola.dashboard.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardWidgetEntity;

public interface DashboardWidgetJpaRepository extends JpaRepository<DashboardWidgetEntity, UUID> {

    Optional<DashboardWidgetEntity> findByDashboardIdAndCodigo(UUID dashboardId, String codigo);

    List<DashboardWidgetEntity> findByDashboardId(UUID dashboardId);
}
