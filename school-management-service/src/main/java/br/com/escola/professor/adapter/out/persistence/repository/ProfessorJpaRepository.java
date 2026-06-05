package br.com.escola.professor.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity;

public interface ProfessorJpaRepository extends JpaRepository<ProfessorEntity, UUID> {

    Optional<ProfessorEntity> findByPessoaId(UUID pessoaId);

    boolean existsByPessoaId(UUID pessoaId);

    List<ProfessorEntity> findByAtivoTrueOrderByCreatedAtAsc();
}
