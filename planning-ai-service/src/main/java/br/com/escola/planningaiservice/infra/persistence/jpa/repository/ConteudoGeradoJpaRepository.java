package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoGeradoJpaEntity;

public interface ConteudoGeradoJpaRepository
        extends JpaRepository<ConteudoGeradoJpaEntity, UUID> {

    List<ConteudoGeradoJpaEntity> findByEscolaIdAndPlanejamentoBimestralIdOrderByCreatedAtAsc(
            UUID escolaId,
            UUID planejamentoBimestralId);

    Optional<ConteudoGeradoJpaEntity> findByIdAndEscolaId(UUID id, UUID escolaId);
}

