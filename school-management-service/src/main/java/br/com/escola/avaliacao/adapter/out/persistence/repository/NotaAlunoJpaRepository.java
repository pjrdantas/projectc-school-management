package br.com.escola.avaliacao.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.avaliacao.adapter.out.persistence.entity.NotaAlunoEntity;

public interface NotaAlunoJpaRepository extends JpaRepository<NotaAlunoEntity, UUID> {

    List<NotaAlunoEntity> findByMatriculaId(UUID matriculaId);

    List<NotaAlunoEntity> findByMatricula_IdAndMatricula_Turma_Escola_Id(UUID matriculaId, UUID escolaId);

    List<NotaAlunoEntity> findByAvaliacaoId(UUID avaliacaoId);

    List<NotaAlunoEntity> findByAvaliacao_IdAndAvaliacao_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
            UUID avaliacaoId,
            UUID escolaId);

    Optional<NotaAlunoEntity> findByAvaliacaoIdAndMatriculaId(UUID avaliacaoId, UUID matriculaId);

    Optional<NotaAlunoEntity> findByAvaliacao_IdAndAvaliacao_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndMatricula_Id(
            UUID avaliacaoId,
            UUID escolaId,
            UUID matriculaId);
}
