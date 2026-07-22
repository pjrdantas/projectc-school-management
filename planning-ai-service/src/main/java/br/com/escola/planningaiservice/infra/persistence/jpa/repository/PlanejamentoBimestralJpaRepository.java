package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanejamentoBimestralJpaEntity;

public interface PlanejamentoBimestralJpaRepository extends JpaRepository<PlanejamentoBimestralJpaEntity, UUID> {

    Optional<PlanejamentoBimestralJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);

    List<PlanejamentoBimestralJpaEntity> findByEscolaIdOrderByCreatedAtDesc(UUID escolaId);

    List<PlanejamentoBimestralJpaEntity> findByEscolaIdAndProfessorTurmaDisciplinaIdOrderByCreatedAtDesc(
            UUID escolaId, UUID professorTurmaDisciplinaId);

    List<PlanejamentoBimestralJpaEntity> findByEscolaIdAndPeriodoAvaliativoIdOrderByCreatedAtDesc(
            UUID escolaId, UUID periodoAvaliativoId);

    List<PlanejamentoBimestralJpaEntity> findByEscolaIdAndProfessorTurmaDisciplinaIdAndPeriodoAvaliativoIdOrderByCreatedAtDesc(
            UUID escolaId, UUID professorTurmaDisciplinaId, UUID periodoAvaliativoId);
}
