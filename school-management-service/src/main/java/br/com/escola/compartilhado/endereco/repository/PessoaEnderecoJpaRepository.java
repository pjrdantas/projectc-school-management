package br.com.escola.compartilhado.endereco.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.compartilhado.endereco.entity.PessoaEnderecoEntity;

public interface PessoaEnderecoJpaRepository extends JpaRepository<PessoaEnderecoEntity, UUID> {

    List<PessoaEnderecoEntity> findByPessoaId(UUID pessoaId);

    @Query("""
            SELECT pe
            FROM PessoaEnderecoEntity pe
            JOIN FETCH pe.endereco
            LEFT JOIN FETCH pe.tipoEndereco
            WHERE pe.pessoa.id = :pessoaId
              AND pe.principal = true
            """)
    Optional<PessoaEnderecoEntity> findPrincipalByPessoaId(UUID pessoaId);

    @Modifying
    void deleteByPessoaId(UUID pessoaId);

    long countByEnderecoId(UUID enderecoId);
}
