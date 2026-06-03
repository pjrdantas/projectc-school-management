package br.com.escola.planejamento.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralAvaliacaoEntity;

public interface PlanejamentoBimestralAvaliacaoJpaRepository extends JpaRepository<PlanejamentoBimestralAvaliacaoEntity, UUID> {

    List<PlanejamentoBimestralAvaliacaoEntity> findByPlanejamentoBimestralId(UUID planejamentoBimestralId);
}
