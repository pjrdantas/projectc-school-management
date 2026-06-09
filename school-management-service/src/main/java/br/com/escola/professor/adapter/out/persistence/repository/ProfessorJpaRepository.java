package br.com.escola.professor.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity;

public interface ProfessorJpaRepository extends JpaRepository<ProfessorEntity, UUID> {

    Optional<ProfessorEntity> findByPessoaId(UUID pessoaId);

    Optional<ProfessorEntity> findByUsuarioId(UUID usuarioId);

    @Query("""
            SELECT p FROM ProfessorEntity p
            JOIN p.pessoa pessoa
            WHERE p.ativo = true
              AND lower(trim(pessoa.email)) = lower(trim(:email))
            """)
    Optional<ProfessorEntity> findAtivoByPessoaEmailIgnoreCase(@Param("email") String email);

    boolean existsByPessoaId(UUID pessoaId);

    List<ProfessorEntity> findByAtivoTrueOrderByCreatedAtAsc();
}
