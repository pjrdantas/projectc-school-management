package br.com.escola.shared.person.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.shared.person.entity.PessoaEntity;
import br.com.escola.shared.person.entity.PessoaTipoPessoaEntity;
import br.com.escola.shared.person.entity.TipoPessoaEntity;

public interface PessoaTipoPessoaJpaRepository extends JpaRepository<PessoaTipoPessoaEntity, UUID> {

    boolean existsByPessoaAndTipoPessoa(PessoaEntity pessoa, TipoPessoaEntity tipoPessoa);
}
