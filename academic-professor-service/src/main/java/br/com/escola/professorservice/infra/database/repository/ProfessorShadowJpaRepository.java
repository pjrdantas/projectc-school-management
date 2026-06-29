package br.com.escola.professorservice.infra.database.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;

public interface ProfessorShadowJpaRepository extends JpaRepository<ProfessorShadowJpaEntity, UUID> {

    Optional<ProfessorShadowJpaEntity> findByPessoaIdAndEscolaId(UUID pessoaId, UUID escolaId);
}
