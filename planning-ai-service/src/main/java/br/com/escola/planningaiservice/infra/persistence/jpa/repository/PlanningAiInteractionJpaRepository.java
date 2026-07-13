package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiInteractionJpaEntity;

public interface PlanningAiInteractionJpaRepository extends JpaRepository<PlanningAiInteractionJpaEntity, UUID> {

    List<PlanningAiInteractionJpaEntity> findByEscolaIdAndPlanejamentoBimestralIdOrderByCreatedAtAsc(
            UUID escolaId,
            UUID planejamentoBimestralId);
}
