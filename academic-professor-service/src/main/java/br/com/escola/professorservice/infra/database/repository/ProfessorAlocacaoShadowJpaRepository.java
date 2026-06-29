package br.com.escola.professorservice.infra.database.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;

public interface ProfessorAlocacaoShadowJpaRepository extends JpaRepository<ProfessorAlocacaoShadowJpaEntity, UUID> {

    Optional<ProfessorAlocacaoShadowJpaEntity> findByProfessorIdAndTurmaDisciplinaId(UUID professorId, UUID turmaDisciplinaId);

    java.util.List<ProfessorAlocacaoShadowJpaEntity> findAllByProfessorIdOrderByCreatedAtAsc(UUID professorId);

    java.util.List<ProfessorAlocacaoShadowJpaEntity> findAllByTurmaIdOrderByCreatedAtAsc(UUID turmaId);
}
