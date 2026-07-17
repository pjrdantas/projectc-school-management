package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.InteracaoJpaEntity;

public interface InteracaoJpaRepository extends JpaRepository<InteracaoJpaEntity, UUID> {

    List<InteracaoJpaEntity> findByEscolaIdAndPlanejamentoBimestralIdOrderByCreatedAtAsc(
            UUID escolaId,
            UUID planejamentoBimestralId);
}

