package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanejamentoBimestralAulaJpaEntity;

public interface PlanejamentoBimestralAulaJpaRepository extends JpaRepository<PlanejamentoBimestralAulaJpaEntity, UUID> {
    Optional<PlanejamentoBimestralAulaJpaEntity> findByPlanejamentoBimestralIdAndNumeroAula(
            UUID planejamentoBimestralId, int numeroAula);
}
