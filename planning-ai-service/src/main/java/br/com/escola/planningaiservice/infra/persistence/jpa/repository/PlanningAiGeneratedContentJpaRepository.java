package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;

public interface PlanningAiGeneratedContentJpaRepository
        extends JpaRepository<PlanningAiGeneratedContentJpaEntity, UUID> {

    List<PlanningAiGeneratedContentJpaEntity> findByEscolaIdAndPlanejamentoBimestralIdOrderByCreatedAtAsc(
            UUID escolaId,
            UUID planejamentoBimestralId);

    Optional<PlanningAiGeneratedContentJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);
}
