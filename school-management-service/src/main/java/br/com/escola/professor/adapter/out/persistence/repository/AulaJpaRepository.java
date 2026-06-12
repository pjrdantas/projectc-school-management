package br.com.escola.professor.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professor.adapter.out.persistence.entity.AulaEntity;

public interface AulaJpaRepository extends JpaRepository<AulaEntity, UUID> {

    List<AulaEntity> findByProfessorTurmaDisciplinaId(UUID professorTurmaDisciplinaId);

    List<AulaEntity> findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID professorTurmaDisciplinaId,
            UUID escolaId);

    List<AulaEntity> findByDataAula(LocalDate dataAula);

    List<AulaEntity> findByProfessorTurmaDisciplinaTurmaDisciplinaTurmaId(UUID turmaId);

    List<AulaEntity> findByProfessorTurmaDisciplina_TurmaDisciplina_Turma_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID turmaId,
            UUID escolaId);

    List<AulaEntity> findAllByProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(UUID escolaId);

    Optional<AulaEntity> findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(UUID id, UUID escolaId);

    boolean existsByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(UUID id, UUID escolaId);
}
