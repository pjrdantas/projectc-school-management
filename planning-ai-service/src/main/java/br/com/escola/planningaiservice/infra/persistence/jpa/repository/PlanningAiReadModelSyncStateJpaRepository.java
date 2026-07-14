package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiReadModelSyncStateJpaEntity;

public interface PlanningAiReadModelSyncStateJpaRepository
        extends JpaRepository<PlanningAiReadModelSyncStateJpaEntity, String> {
}
