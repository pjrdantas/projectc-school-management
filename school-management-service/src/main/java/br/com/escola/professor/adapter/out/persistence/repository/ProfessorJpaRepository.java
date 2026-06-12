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

    Optional<ProfessorEntity> findByPessoa_IdAndPessoa_Escola_Id(UUID pessoaId, UUID escolaId);

    Optional<ProfessorEntity> findByUsuarioId(UUID usuarioId);

    Optional<ProfessorEntity> findByUsuario_IdAndPessoa_Escola_Id(UUID usuarioId, UUID escolaId);

    @Query("""
            SELECT p FROM ProfessorEntity p
            JOIN p.pessoa pessoa
            WHERE p.ativo = true
              AND lower(trim(pessoa.email)) = lower(trim(:email))
            """)
    Optional<ProfessorEntity> findAtivoByPessoaEmailIgnoreCase(@Param("email") String email);

    @Query("""
            SELECT p FROM ProfessorEntity p
            JOIN p.pessoa pessoa
            WHERE p.ativo = true
              AND pessoa.escola.id = :escolaId
              AND lower(trim(pessoa.email)) = lower(trim(:email))
            """)
    Optional<ProfessorEntity> findAtivoByPessoaEmailIgnoreCaseAndEscolaId(
            @Param("email") String email,
            @Param("escolaId") UUID escolaId);

    boolean existsByPessoaId(UUID pessoaId);

    boolean existsByPessoa_IdAndPessoa_Escola_Id(UUID pessoaId, UUID escolaId);

    boolean existsByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    Optional<ProfessorEntity> findByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    List<ProfessorEntity> findAllByPessoa_Escola_Id(UUID escolaId);

    List<ProfessorEntity> findByAtivoTrueOrderByCreatedAtAsc();

    List<ProfessorEntity> findByAtivoTrueAndPessoa_Escola_IdOrderByCreatedAtAsc(UUID escolaId);
}
