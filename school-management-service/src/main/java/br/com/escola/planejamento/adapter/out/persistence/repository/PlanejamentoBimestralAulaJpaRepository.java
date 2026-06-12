package br.com.escola.planejamento.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralAulaEntity;

public interface PlanejamentoBimestralAulaJpaRepository extends JpaRepository<PlanejamentoBimestralAulaEntity, UUID> {

    List<PlanejamentoBimestralAulaEntity> findByPlanejamentoBimestralId(UUID planejamentoBimestralId);

    Optional<PlanejamentoBimestralAulaEntity> findByPlanejamentoBimestralIdAndNumeroAula(
            UUID planejamentoBimestralId,
            Integer numeroAula);
}
