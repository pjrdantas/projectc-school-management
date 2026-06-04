package br.com.escola.dashboard.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardIndicadorSnapshotEntity;

public interface DashboardIndicadorSnapshotJpaRepository
        extends JpaRepository<DashboardIndicadorSnapshotEntity, UUID> {

    Optional<DashboardIndicadorSnapshotEntity> findByPublicoDashboardIdAndCodigoIndicadorAndReferenciaData(
            UUID publicoDashboardId,
            String codigoIndicador,
            LocalDate referenciaData);

    List<DashboardIndicadorSnapshotEntity> findByPublicoDashboardId(UUID publicoDashboardId);

    List<DashboardIndicadorSnapshotEntity> findByPublicoDashboardIdOrderByReferenciaDataDescCodigoIndicadorAsc(
            UUID publicoDashboardId);

    List<DashboardIndicadorSnapshotEntity> findByPublicoDashboardIdAndReferenciaDataOrderByCodigoIndicadorAsc(
            UUID publicoDashboardId,
            LocalDate referenciaData);
}
