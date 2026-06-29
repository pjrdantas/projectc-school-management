package br.com.escola.professorservice.infra.database.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;

public interface ProfessorAlocacaoShadowJpaRepository extends JpaRepository<ProfessorAlocacaoShadowJpaEntity, UUID> {

    Optional<ProfessorAlocacaoShadowJpaEntity> findByProfessorIdAndTurmaDisciplinaId(UUID professorId, UUID turmaDisciplinaId);
}
