package br.com.escola.planejamento.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;

public interface PlanejamentoBimestralJpaRepository extends JpaRepository<PlanejamentoBimestralEntity, UUID> {

    List<PlanejamentoBimestralEntity> findByProfessorTurmaDisciplinaId(UUID professorTurmaDisciplinaId);
}
