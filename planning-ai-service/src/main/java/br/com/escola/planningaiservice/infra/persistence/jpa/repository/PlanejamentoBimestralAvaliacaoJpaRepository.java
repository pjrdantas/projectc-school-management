package br.com.escola.planningaiservice.infra.persistence.jpa.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanejamentoBimestralAvaliacaoJpaEntity;

public interface PlanejamentoBimestralAvaliacaoJpaRepository extends JpaRepository<PlanejamentoBimestralAvaliacaoJpaEntity, UUID> {
    Optional<PlanejamentoBimestralAvaliacaoJpaEntity> findByPlanejamentoBimestralIdAndChaveIdempotencia(
            UUID planejamentoBimestralId, String chaveIdempotencia);
}
