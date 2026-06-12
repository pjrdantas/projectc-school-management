package br.com.escola.dashboard.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardIndicadorSnapshotEntity;

public interface DashboardIndicadorSnapshotJpaRepository
        extends JpaRepository<DashboardIndicadorSnapshotEntity, UUID> {

    Optional<DashboardIndicadorSnapshotEntity> findByPublicoDashboardIdAndEscola_IdAndCodigoIndicadorAndReferenciaData(
            UUID publicoDashboardId,
            UUID escolaId,
            String codigoIndicador,
            LocalDate referenciaData);

    List<DashboardIndicadorSnapshotEntity> findByPublicoDashboardIdAndEscola_IdOrderByReferenciaDataDescCodigoIndicadorAsc(
            UUID publicoDashboardId,
            UUID escolaId);

    List<DashboardIndicadorSnapshotEntity> findByPublicoDashboardIdAndEscola_IdAndReferenciaDataOrderByCodigoIndicadorAsc(
            UUID publicoDashboardId,
            UUID escolaId,
            LocalDate referenciaData);
}
