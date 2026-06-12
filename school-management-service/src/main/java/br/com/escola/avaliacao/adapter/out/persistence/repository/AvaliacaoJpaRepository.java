package br.com.escola.avaliacao.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.avaliacao.adapter.out.persistence.entity.AvaliacaoEntity;

public interface AvaliacaoJpaRepository extends JpaRepository<AvaliacaoEntity, UUID> {

    List<AvaliacaoEntity> findByProfessorTurmaDisciplinaId(UUID professorTurmaDisciplinaId);

    List<AvaliacaoEntity> findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID professorTurmaDisciplinaId,
            UUID escolaId);

    List<AvaliacaoEntity> findByProfessorTurmaDisciplinaTurmaDisciplinaTurmaId(UUID turmaId);

    List<AvaliacaoEntity> findByProfessorTurmaDisciplina_TurmaDisciplina_Turma_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID turmaId,
            UUID escolaId);

    List<AvaliacaoEntity> findAllByProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(UUID escolaId);

    Optional<AvaliacaoEntity> findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(UUID id, UUID escolaId);

    boolean existsByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(UUID id, UUID escolaId);
}
