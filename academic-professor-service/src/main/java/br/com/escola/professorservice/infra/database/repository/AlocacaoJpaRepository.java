package br.com.escola.professorservice.infra.database.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;

public interface AlocacaoJpaRepository extends JpaRepository<AlocacaoJpaEntity, UUID> {

    Optional<AlocacaoJpaEntity> findByProfessorIdAndTurmaDisciplinaId(UUID professorId, UUID turmaDisciplinaId);

    boolean existsByProfessorIdAndTurmaDisciplinaIdAndAtivoTrue(UUID professorId, UUID turmaDisciplinaId);

    Optional<AlocacaoJpaEntity> findByProfessorIdAndTurmaDisciplinaIdAndAtivoTrue(
            UUID professorId,
            UUID turmaDisciplinaId);

    boolean existsByProfessorIdAndAtivoTrue(UUID professorId);

    java.util.List<AlocacaoJpaEntity> findAllByProfessorIdOrderByCreatedAtAsc(UUID professorId);

    java.util.List<AlocacaoJpaEntity> findAllByTurmaIdOrderByCreatedAtAsc(UUID turmaId);
}

