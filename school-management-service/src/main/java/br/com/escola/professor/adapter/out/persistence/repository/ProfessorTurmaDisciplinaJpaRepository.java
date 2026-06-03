package br.com.escola.professor.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;

public interface ProfessorTurmaDisciplinaJpaRepository extends JpaRepository<ProfessorTurmaDisciplinaEntity, UUID> {

    List<ProfessorTurmaDisciplinaEntity> findByProfessorId(UUID professorId);
}
