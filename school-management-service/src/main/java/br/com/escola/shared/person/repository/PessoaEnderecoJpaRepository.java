package br.com.escola.shared.person.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.shared.person.entity.PessoaEnderecoEntity;

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
}
