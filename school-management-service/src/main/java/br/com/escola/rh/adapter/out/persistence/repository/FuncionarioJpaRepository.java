package br.com.escola.rh.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;

public interface FuncionarioJpaRepository extends JpaRepository<FuncionarioEntity, UUID> {

    Optional<FuncionarioEntity> findByPessoaId(UUID pessoaId);

    Optional<FuncionarioEntity> findByIdAndPessoa_Escola_Id(UUID id, UUID escolaId);

    List<FuncionarioEntity> findAllByPessoa_Escola_Id(UUID escolaId);
}
