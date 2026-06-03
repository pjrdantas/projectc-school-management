package br.com.escola.planejamento.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoAulaEntity;

public interface PlanejamentoAulaJpaRepository extends JpaRepository<PlanejamentoAulaEntity, UUID> {

    List<PlanejamentoAulaEntity> findByPlanejamentoProfessorId(UUID planejamentoProfessorId);
}
