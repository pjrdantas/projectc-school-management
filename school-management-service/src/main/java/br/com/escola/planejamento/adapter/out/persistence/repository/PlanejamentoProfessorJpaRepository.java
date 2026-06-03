package br.com.escola.planejamento.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoProfessorEntity;

public interface PlanejamentoProfessorJpaRepository extends JpaRepository<PlanejamentoProfessorEntity, UUID> {

    List<PlanejamentoProfessorEntity> findByProfessorTurmaDisciplinaId(UUID professorTurmaDisciplinaId);
}
