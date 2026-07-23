package br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelIndicadorSnapshotJpaEntity;

public interface PainelIndicadorSnapshotJpaRepository extends JpaRepository<PainelIndicadorSnapshotJpaEntity, UUID> {

    Optional<PainelIndicadorSnapshotJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    Optional<PainelIndicadorSnapshotJpaEntity> findByEscolaIdAndPublicoIdAndCodigoIndicadorAndReferenciaData(
            UUID escolaId, UUID publicoId, String codigoIndicador, LocalDate referenciaData);

    List<PainelIndicadorSnapshotJpaEntity> findByEscolaIdAndPublicoIdOrderByCodigoIndicadorAscReferenciaDataAsc(
            UUID escolaId, UUID publicoId);

    List<PainelIndicadorSnapshotJpaEntity> findByEscolaIdAndPublicoIdAndReferenciaDataOrderByCodigoIndicadorAsc(
            UUID escolaId, UUID publicoId, LocalDate referenciaData);
}
