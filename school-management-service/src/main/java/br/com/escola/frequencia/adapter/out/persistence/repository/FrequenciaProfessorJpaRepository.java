package br.com.escola.frequencia.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaProfessorEntity;

public interface FrequenciaProfessorJpaRepository extends JpaRepository<FrequenciaProfessorEntity, UUID> {

    List<FrequenciaProfessorEntity> findByProfessorId(UUID professorId);

    List<FrequenciaProfessorEntity> findByAulaId(UUID aulaId);

    List<FrequenciaProfessorEntity> findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID aulaId,
            UUID escolaId);

    Optional<FrequenciaProfessorEntity> findByAulaIdAndProfessorId(UUID aulaId, UUID professorId);

    Optional<FrequenciaProfessorEntity> findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndProfessor_Id(
            UUID aulaId,
            UUID escolaId,
            UUID professorId);
}
