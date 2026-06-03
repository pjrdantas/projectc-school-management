package br.com.escola.rh.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;

public interface FuncionarioJpaRepository extends JpaRepository<FuncionarioEntity, UUID> {

    Optional<FuncionarioEntity> findByPessoaId(UUID pessoaId);
}
