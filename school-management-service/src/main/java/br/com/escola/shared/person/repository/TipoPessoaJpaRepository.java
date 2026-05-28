package br.com.escola.shared.person.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.escola.shared.person.entity.TipoPessoaEntity;

public interface TipoPessoaJpaRepository extends JpaRepository<TipoPessoaEntity, UUID> {

    Optional<TipoPessoaEntity> findByCodigo(String codigo);
}
