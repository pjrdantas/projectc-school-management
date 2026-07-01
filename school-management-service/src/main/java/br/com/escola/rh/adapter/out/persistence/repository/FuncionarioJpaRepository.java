package br.com.escola.rh.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;

public interface FuncionarioJpaRepository extends JpaRepository<FuncionarioEntity, UUID> {

    Optional<FuncionarioEntity> findByPessoaId(UUID pessoaId);

    Optional<FuncionarioEntity> findByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    List<FuncionarioEntity> findAllByPessoa_Escola_Id(UUID escolaId);

    @Query("""
            SELECT f FROM FuncionarioEntity f
            JOIN FETCH f.pessoa p
            LEFT JOIN FETCH f.cargo c
            WHERE f.ativo = true
              AND p.ativo = true
              AND p.escola.id = :escolaId
              AND lower(trim(p.email)) = lower(trim(:email))
            """)
    List<FuncionarioEntity> findAtivosByPessoaEmailAndEscolaId(
            @Param("email") String email,
            @Param("escolaId") UUID escolaId);
}
