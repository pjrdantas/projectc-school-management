package br.com.escola.professor.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professor.adapter.out.persistence.entity.AulaEntity;

public interface AulaJpaRepository extends JpaRepository<AulaEntity, UUID> {

    List<AulaEntity> findByProfessorTurmaDisciplinaId(UUID professorTurmaDisciplinaId);

    List<AulaEntity> findByDataAula(LocalDate dataAula);

    List<AulaEntity> findByProfessorTurmaDisciplinaTurmaDisciplinaTurmaId(UUID turmaId);
}
