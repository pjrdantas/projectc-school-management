package br.com.escola.dashboard.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardEntity;

public interface DashboardJpaRepository extends JpaRepository<DashboardEntity, UUID> {

    Optional<DashboardEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<DashboardEntity> findAllByOrderByNomeAsc();

    List<DashboardEntity> findByPublicoDashboardId(UUID publicoDashboardId);

    List<DashboardEntity> findByPublicoDashboardIdOrderByNomeAsc(UUID publicoDashboardId);
}
