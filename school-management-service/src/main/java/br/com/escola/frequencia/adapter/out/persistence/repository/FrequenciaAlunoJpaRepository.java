package br.com.escola.frequencia.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;

public interface FrequenciaAlunoJpaRepository extends JpaRepository<FrequenciaAlunoEntity, UUID> {

    List<FrequenciaAlunoEntity> findByMatriculaId(UUID matriculaId);

    List<FrequenciaAlunoEntity> findByMatricula_IdAndMatricula_Turma_Escola_Id(UUID matriculaId, UUID escolaId);

    List<FrequenciaAlunoEntity> findByAulaId(UUID aulaId);

    List<FrequenciaAlunoEntity> findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID aulaId,
            UUID escolaId);

    Optional<FrequenciaAlunoEntity> findByAulaIdAndMatriculaId(UUID aulaId, UUID matriculaId);

    Optional<FrequenciaAlunoEntity> findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndMatricula_Id(
            UUID aulaId,
            UUID escolaId,
            UUID matriculaId);
}
