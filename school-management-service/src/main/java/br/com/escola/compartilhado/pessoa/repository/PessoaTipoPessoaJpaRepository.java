package br.com.escola.compartilhado.pessoa.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import br.com.escola.compartilhado.pessoa.entity.PessoaEntity;
import br.com.escola.compartilhado.pessoa.entity.PessoaTipoPessoaEntity;
import br.com.escola.compartilhado.pessoa.entity.TipoPessoaEntity;

public interface PessoaTipoPessoaJpaRepository extends JpaRepository<PessoaTipoPessoaEntity, UUID> {

    boolean existsByPessoaAndTipoPessoa(PessoaEntity pessoa, TipoPessoaEntity tipoPessoa);

    @Modifying
    void deleteByPessoaId(UUID pessoaId);
}
