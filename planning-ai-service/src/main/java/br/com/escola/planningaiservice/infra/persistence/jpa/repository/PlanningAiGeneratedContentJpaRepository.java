package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;

public interface PlanningAiGeneratedContentJpaRepository
        extends JpaRepository<PlanningAiGeneratedContentJpaEntity, UUID> {
}
