package br.com.escola.planejamento.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;

public interface PlanejamentoBimestralJpaRepository extends JpaRepository<PlanejamentoBimestralEntity, UUID> {

    List<PlanejamentoBimestralEntity> findByProfessorTurmaDisciplinaId(UUID professorTurmaDisciplinaId);

    @Query("""
            SELECT planejamento
            FROM PlanejamentoBimestralEntity planejamento
            JOIN planejamento.professorTurmaDisciplina alocacao
            JOIN alocacao.professor professor
            JOIN alocacao.turmaDisciplina turmaDisciplina
            JOIN turmaDisciplina.turma turma
            JOIN turmaDisciplina.disciplina disciplina
            WHERE (:professorId IS NULL OR professor.id = :professorId)
              AND (:turmaId IS NULL OR turma.id = :turmaId)
              AND (:disciplinaId IS NULL OR disciplina.id = :disciplinaId)
              AND (:periodoAvaliativoId IS NULL OR planejamento.periodoAvaliativo.id = :periodoAvaliativoId)
            ORDER BY planejamento.createdAt DESC
            """)
    List<PlanejamentoBimestralEntity> filtrar(
            @Param("professorId") UUID professorId,
            @Param("turmaId") UUID turmaId,
            @Param("disciplinaId") UUID disciplinaId,
            @Param("periodoAvaliativoId") UUID periodoAvaliativoId);
}
