package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiContentVersionJpaEntity;

public interface PlanningAiContentVersionJpaRepository extends JpaRepository<PlanningAiContentVersionJpaEntity, UUID> {

    List<PlanningAiContentVersionJpaEntity> findByEscolaIdAndConteudoGerado_IdOrderByNumeroVersaoAsc(
            UUID escolaId,
            UUID conteudoGeradoId);
}
